// 허용 목록에 있는 공개 파일만 dist/ 로 복사합니다.
// application.properties, .metadata, 원본 jar, Gradle 빌드 산출물은 절대 포함하지 않습니다.
import { copyFileSync, mkdirSync, readdirSync, rmSync, statSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const DEMO_ROOT = resolve(here, '..');
const SRC = join(DEMO_ROOT, 'src');
export const DIST = join(DEMO_ROOT, 'dist');

export const PUBLIC_FILES = ['favicon.ico', 'index.html', 'styles.css', 'app.js', 'mock-adapter.js', 'keyword-store.js', 'generated/contract.js'];

export function listFiles(dir, root = dir) {
  return readdirSync(dir).flatMap((name) => {
    const full = join(dir, name);
    return statSync(full).isDirectory() ? listFiles(full, root) : [relative(root, full).split('\\').join('/')];
  });
}

export function build() {
  rmSync(DIST, { recursive: true, force: true });
  for (const file of PUBLIC_FILES) {
    const target = join(DIST, file);
    mkdirSync(dirname(target), { recursive: true });
    copyFileSync(join(SRC, file), target);
  }
  const written = listFiles(DIST).sort();
  const expected = [...PUBLIC_FILES].sort();
  if (JSON.stringify(written) !== JSON.stringify(expected)) {
    throw new Error(`dist 파일 목록이 허용 목록과 다릅니다: ${written.join(', ')}`);
  }
  return written;
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const files = build();
  console.log(`dist/ 에 ${files.length}개 파일을 만들었습니다: ${files.join(', ')}`);
}
