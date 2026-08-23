import type { Config } from 'tailwindcss';

const config: Config = {
  darkMode: ['class'],
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        background: '#0A0F0D',
        foreground: '#F5F7F6',
        card: {
          DEFAULT: '#121815',
          foreground: '#F5F7F6',
        },
        popover: {
          DEFAULT: '#121815',
          foreground: '#F5F7F6',
        },
        primary: {
          DEFAULT: '#00C896',
          foreground: '#0A0F0D',
          hover: '#00B084',
        },
        secondary: {
          DEFAULT: '#1A231F',
          foreground: '#E1E7E4',
        },
        muted: {
          DEFAULT: '#151D19',
          foreground: '#8A9992',
        },
        accent: {
          DEFAULT: '#00C896',
          foreground: '#0A0F0D',
        },
        destructive: {
          DEFAULT: '#EF4444',
          foreground: '#FFFFFF',
        },
        warning: {
          DEFAULT: '#F59E0B',
          foreground: '#0A0F0D',
        },
        success: {
          DEFAULT: '#10B981',
          foreground: '#0A0F0D',
        },
        border: '#1E2B25',
        input: '#1A231F',
        ring: '#00C896',
      },
      borderRadius: {
        lg: '12px',
        md: '8px',
        sm: '6px',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
};

export default config;
