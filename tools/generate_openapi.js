const fs = require('fs');
const path = require('path');

function readAllControllerFiles(controllerDir) {
  const entries = fs.readdirSync(controllerDir, { withFileTypes: true });
  const files = [];
  for (const e of entries) {
    const p = path.join(controllerDir, e.name);
    if (e.isDirectory()) files.push(...readAllControllerFiles(p));
    else if (e.isFile() && p.endsWith('.java')) files.push(p);
  }
  return files;
}

function trimQuotes(s) {
  return s.replace(/^"|"$/g, '');
}

function normalizePath(p) {
  if (!p) return '';
  let x = p.trim();
  x = trimQuotes(x);
  if (!x.startsWith('/')) x = '/' + x;
  x = x.replace(/\/+/g, '/');
  if (x !== '/' && x.endsWith('/')) x = x.slice(0, -1);
  return x;
}

function joinPaths(a, b) {
  const aa = normalizePath(a || '');
  const bb = normalizePath(b || '');
  if (aa === '/') return bb || '/';
  if (!aa) return bb || '/';
  if (!bb || bb === '/') return aa || '/';
  return normalizePath(aa + '/' + bb);
}

function extractFirstMatch(src, re) {
  const m = src.match(re);
  return m ? m[1] : null;
}

function extractRequestMappingBase(src) {
  // @RequestMapping("/xxx")
  const base = extractFirstMatch(src, /@RequestMapping\(\s*(".*?")\s*\)/);
  return base ? trimQuotes(base) : '';
}

function extractTagName(src) {
  const tag = extractFirstMatch(src, /@Tag\(\s*name\s*=\s*(".*?")/);
  return tag ? trimQuotes(tag) : null;
}

function parseControllerFile(filePath) {
  const src = fs.readFileSync(filePath, 'utf8');
  const base = extractRequestMappingBase(src);
  const tag = extractTagName(src) || path.basename(filePath, '.java');

  // Find methods by scanning annotations blocks.
  // This is intentionally simple; it covers your codebase style.
  const lines = src.split('\n');
  const ops = [];
  let buffer = [];
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (line.trim().startsWith('@')) {
      buffer.push(line.trim());
      continue;
    }
    // method signature line (public Result<...> xxx(...)
    if (buffer.length > 0 && /\bpublic\s+Result\b/.test(line)) {
      const ann = buffer.join(' ');
      buffer = [];

      const opSummary = extractFirstMatch(ann, /@Operation\(\s*summary\s*=\s*(".*?")/);
      const opDesc = extractFirstMatch(ann, /@Operation\(\s*summary\s*=\s*".*?"\s*,\s*description\s*=\s*(".*?")/);

      const methodMap = [
        ['get', /@GetMapping\((.*?)\)/],
        ['post', /@PostMapping\((.*?)\)/],
        ['put', /@PutMapping\((.*?)\)/],
        ['patch', /@PatchMapping\((.*?)\)/],
        ['delete', /@DeleteMapping\((.*?)\)/],
      ];
      let httpMethod = null;
      let subPath = '';
      for (const [m, re] of methodMap) {
        const mm = ann.match(re);
        if (mm) {
          httpMethod = m;
          const inside = (mm[1] || '').trim();
          // handles @GetMapping or @GetMapping("/x")
          if (!inside) subPath = '';
          else {
            const p = extractFirstMatch(inside, /^\s*(".*?")\s*$/);
            subPath = p ? trimQuotes(p) : '';
          }
          break;
        }
      }
      // @GetMapping without parentheses
      if (!httpMethod) {
        if (/@GetMapping\b/.test(ann)) httpMethod = 'get';
        else if (/@PostMapping\b/.test(ann)) httpMethod = 'post';
        else if (/@PutMapping\b/.test(ann)) httpMethod = 'put';
        else if (/@PatchMapping\b/.test(ann)) httpMethod = 'patch';
        else if (/@DeleteMapping\b/.test(ann)) httpMethod = 'delete';
      }

      if (!httpMethod) continue;

      const fullPath = joinPaths(base, subPath);
      const summary = opSummary ? trimQuotes(opSummary) : `${tag} ${httpMethod.toUpperCase()} ${fullPath}`;
      const description = opDesc ? trimQuotes(opDesc) : undefined;

      ops.push({ tag, httpMethod, fullPath, summary, description });
    } else {
      buffer = [];
    }
  }

  return ops;
}

function buildOpenApi(ops) {
  const paths = {};
  for (const op of ops) {
    if (!paths[op.fullPath]) paths[op.fullPath] = {};
    paths[op.fullPath][op.httpMethod] = {
      tags: [op.tag],
      summary: op.summary,
      description: op.description,
      responses: {
        '200': {
          description: 'OK',
          content: {
            'application/json': {
              schema: { type: 'object' },
              examples: {
                success: { value: { code: 1, msg: 'success', data: {} } }
              }
            }
          }
        }
      },
      parameters: [
        {
          name: 'token',
          in: 'header',
          required: false,
          schema: { type: 'string' },
          description: 'JWT token（项目使用请求头 token）'
        }
      ]
    };
  }

  // Tag list
  const tagNames = [...new Set(ops.map(o => o.tag))].sort();
  const tags = tagNames.map(name => ({ name }));

  return {
    openapi: '3.0.3',
    info: {
      title: 'demo API (generated from code)',
      version: '1.0.0'
    },
    servers: [{ url: 'http://localhost:8080' }],
    tags,
    paths
  };
}

function main() {
  const controllerDir = path.resolve(process.cwd(), 'src/main/java/com/example/demo/controller');
  const files = readAllControllerFiles(controllerDir);
  const ops = files.flatMap(parseControllerFile);
  const doc = buildOpenApi(ops);
  fs.writeFileSync(path.resolve(process.cwd(), 'openapi.generated.json'), JSON.stringify(doc, null, 2), 'utf8');
  console.log(`generated ${ops.length} operations -> openapi.generated.json`);
}

main();

