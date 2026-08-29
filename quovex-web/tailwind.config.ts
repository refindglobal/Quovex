import type { Config } from 'tailwindcss';

const config: Config = {
  darkMode: ['class', '[data-theme="dark"]'],
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        background: 'var(--background)',
        surface: {
          DEFAULT: 'var(--surface)',
          variant: 'var(--surface-variant)',
          elevated: 'var(--surface-elevated)',
        },
        primary: {
          DEFAULT: 'var(--primary)',
          container: 'var(--primary-container)',
          glow: 'var(--primary-glow)',
          foreground: 'var(--on-primary)',
        },
        secondary: {
          DEFAULT: 'var(--secondary)',
          foreground: 'var(--on-secondary)',
        },
        border: {
          DEFAULT: 'var(--border)',
          light: 'var(--border-light)',
          focus: 'var(--border-focus)',
        },
        'text-primary': 'var(--text-primary)',
        'text-secondary': 'var(--text-secondary)',
        'text-tertiary': 'var(--text-tertiary)',
        'text-disabled': 'var(--text-disabled)',
        on: {
          background: 'var(--on-background)',
          surface: 'var(--on-surface)',
          'surface-variant': 'var(--on-surface-variant)',
        },
        xp: {
          gold: 'var(--xp-gold)',
        },
        streak: {
          fire: 'var(--streak-fire)',
        },
        success: {
          DEFAULT: 'var(--success)',
          container: 'var(--success-container)',
        },
        warning: {
          DEFAULT: 'var(--warning)',
          container: 'var(--warning-container)',
        },
        error: {
          DEFAULT: 'var(--error)',
          container: 'var(--error-container)',
        },
        destructive: 'var(--error)',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      fontSize: {
        display: ['clamp(2.5rem, 5vw + 1rem, 4rem)', { lineHeight: '1.1', letterSpacing: '-0.02em', fontWeight: '900' }],
        headline: ['clamp(1.75rem, 3vw + 1rem, 2.5rem)', { lineHeight: '1.2', letterSpacing: '-0.01em', fontWeight: '800' }],
        title: ['clamp(1.25rem, 2vw + 1rem, 1.5rem)', { lineHeight: '1.3', letterSpacing: '-0.01em', fontWeight: '700' }],
        section: ['clamp(1rem, 1.5vw + 0.5rem, 1.125rem)', { lineHeight: '1.4', fontWeight: '600' }],
        body: ['0.875rem', { lineHeight: '1.5', fontWeight: '400' }],
        label: ['0.75rem', { lineHeight: '1.5', fontWeight: '600', letterSpacing: '0.02em' }],
        caption: ['0.625rem', { lineHeight: '1.5', fontWeight: '500', letterSpacing: '0.01em' }],
      },
      spacing: {
        'micro': '4px',
        'tiny': '8px',
        'small': '12px',
        'base': '16px',
        'large': '24px',
        'xl': '32px',
        '2xl': '48px',
        '3xl': '64px',
      },
      boxShadow: {
        card: 'var(--card-shadow)',
        'card-hover': '0 8px 32px var(--primary-glow)',
        elevated: '0 12px 48px rgba(0, 0, 0, 0.12)',
        modal: '0 24px 64px rgba(0, 0, 0, 0.2)',
        glow: 'var(--glow-shadow)',
        'glow-lg': '0 0 48px var(--primary-glow)',
      },
      borderRadius: {
        sm: '8px',
        md: '16px',
        lg: '24px',
        xl: '32px',
      },
    },
  },
  plugins: [],
};

export default config;
