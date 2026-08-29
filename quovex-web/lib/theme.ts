'use client';

export type ThemeMode = 'DARK' | 'LIGHT' | 'SYSTEM';
export type ResolvedTheme = 'dark' | 'light';

export const THEME_STORAGE_KEY = 'quovex_theme_mode';

export function getStoredThemeMode(): ThemeMode {
  if (typeof window === 'undefined') return 'DARK';
  try {
    const saved = localStorage.getItem(THEME_STORAGE_KEY) as ThemeMode | null;
    if (saved === 'DARK' || saved === 'LIGHT' || saved === 'SYSTEM') {
      return saved;
    }
  } catch (_) {}
  return 'DARK';
}

export function setStoredThemeMode(mode: ThemeMode): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(THEME_STORAGE_KEY, mode);
  } catch (_) {}
}

export function getSystemTheme(): ResolvedTheme {
  if (typeof window === 'undefined') return 'dark';
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

export function resolveTheme(mode: ThemeMode): ResolvedTheme {
  if (mode === 'SYSTEM') {
    return getSystemTheme();
  }
  return mode === 'LIGHT' ? 'light' : 'dark';
}
