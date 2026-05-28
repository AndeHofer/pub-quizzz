const GITHUB_REPOSITORY_URL = 'https://github.com/AndeHofer/pub-quizzz';

export function buildVersionBadgeMarkup(version: string): string {
    const label = `v${version}`;
    return `<a href="${GITHUB_REPOSITORY_URL}" target="_blank" rel="noopener noreferrer">${label}</a>`;
}
