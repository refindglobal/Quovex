'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import {
  ThemeMode,
  ResolvedTheme,
  getStoredThemeMode,
  setStoredThemeMode,
  resolveTheme,
} from '@/lib/theme';

interface ThemeContextType {
  themeMode: ThemeMode;
  resolvedTheme: ResolvedTheme;
  setThemeMode: (mode: ThemeMode) => void;
}

const ThemeContext = createContext<ThemeContextType>({
  themeMode: 'DARK',
  resolvedTheme: 'dark',
  setThemeMode: () => {},
});

export const useTheme = () => useContext(ThemeContext);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [themeMode, setThemeState] = useState<ThemeMode>('DARK');
  const [resolvedTheme, setResolvedTheme] = useState<ResolvedTheme>('dark');
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    const initialMode = getStoredThemeMode();
    const resolved = resolveTheme(initialMode);
    setThemeState(initialMode);
    setResolvedTheme(resolved);
    applyThemeToDocument(resolved);
    setMounted(true);

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = () => {
      const currentMode = getStoredThemeMode();
      if (currentMode === 'SYSTEM') {
        const sysResolved = resolveTheme('SYSTEM');
        setResolvedTheme(sysResolved);
        applyThemeToDocument(sysResolved);
      }
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  const applyThemeToDocument = (theme: ResolvedTheme) => {
    const root = document.documentElement;
    if (theme === 'light') {
      root.setAttribute('data-theme', 'light');
      root.classList.remove('dark');
      root.classList.add('light');
    } else {
      root.setAttribute('data-theme', 'dark');
      root.classList.remove('light');
      root.classList.add('dark');
    }
  };

  const handleSetThemeMode = (mode: ThemeMode) => {
    setThemeState(mode);
    setStoredThemeMode(mode);
    const resolved = resolveTheme(mode);
    setResolvedTheme(resolved);
    applyThemeToDocument(resolved);
  };

  return (
    <ThemeContext.Provider
      value={{
        themeMode,
        resolvedTheme,
        setThemeMode: handleSetThemeMode,
      }}
    >
      {children}
    </ThemeContext.Provider>
  );
};
