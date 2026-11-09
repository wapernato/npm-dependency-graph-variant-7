/** Optional verification only; the Java analyzer never invokes npm. */
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const cp = require('node:child_process');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname, '..');
const out = path.join(root, 'output', 'comparison');
fs.mkdirSync(out, {recursive: true});
const java = process.env.JAVA_HOME
  ? path.join(process.env.JAVA_HOME, 'bin', 'java') : 'java';
const prefix = ['-cp', path.join(root, 'build', 'classes'),
  'ru.mirea.graph.Main'];

function run(command, args, cwd) {
  const result = cp.spawnSync(command, args, {
    cwd, encoding: 'utf8', timeout: 180000, maxBuffer: 20000000
  });
  if (result.error) throw result.error;
  if (result.status !== 0) throw Error(result.stderr || result.stdout);
  return result.stdout;
}

function nativeGraph(name, item) {
  const result = {};
  const queue = [[name, item]];
  while (queue.length) {
    const [key, node] = queue.shift();
    const children = Object.entries(node.dependencies || {});
    result[key + '@' + node.version] = children.map(
      ([k, v]) => k + '@' + v.version).sort();
    queue.push(...children);
  }
  return result;
}

function ownGraph(reference) {
  const text = run(java, prefix.concat([
    '--package', reference, '--mode', 'graph'
  ]), root);
  const result = {};
  for (const line of text.split('\n').filter(line => line.length)) {
    const [name, children = ''] = line.split(' -> ');
    result[name] = children ? children.split(', ').sort() : [];
  }
  return result;
}

function compare(reference) {
  const cut = reference.lastIndexOf('@');
  const name = reference.slice(0, cut);
  const version = reference.slice(cut + 1);
  const folder = fs.mkdtempSync(path.join(os.tmpdir(), 'npm-graph-'));
  try {
    fs.writeFileSync(path.join(folder, 'package.json'), JSON.stringify({
      name: 'graph-check', version: '1.0.0', private: true,
      dependencies: {[name]: version}
    }));
    run('npm', ['install', '--ignore-scripts', '--no-audit',
      '--no-fund', '--omit=optional', '--legacy-peer-deps'], folder);
    const native = run('npm', ['ls', '--all', '--json'], folder);
    const actual = JSON.parse(native).dependencies[name];
    assert.deepEqual(ownGraph(reference), nativeGraph(name, actual));
    const tree = run('npm', ['ls', '--all'], folder);
    fs.writeFileSync(path.join(out, name + '.txt'), tree);
    fs.writeFileSync(path.join(out, name + '.json'), native);
    console.log(reference + ': nodes and edges match npm ls');
  } finally {
    fs.rmSync(folder, {recursive: true, force: true});
  }
}

for (const reference of ['is-odd@3.0.1', 'is-even@1.0.0',
  'ansi-styles@4.3.0']) compare(reference);
