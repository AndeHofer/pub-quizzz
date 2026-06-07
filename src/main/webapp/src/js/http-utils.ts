export async function readHttpErrorMessage(response: Response, fallback: string): Promise<string> {
    const bodyText = await response.text().catch(() => '');
    const details = bodyText.trim() || `${response.status} ${response.statusText}`.trim() || `HTTP ${response.status}`;
    return `${fallback}: ${details}`;
}
