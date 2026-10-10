/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        paper: '#f3efe6',
        ink: '#1b1916',
        'ink-soft': '#6d675e',
        line: '#e4dccf',
        accent: '#c4552a',
        'accent-dark': '#9a3e1c',
        sidebar: '#171512',
      },
      fontFamily: {
        sans: ['Outfit', 'ui-sans-serif', 'sans-serif'],
        serif: ['"Source Serif 4"', 'Georgia', 'serif'],
      },
      boxShadow: {
        card: '0 1px 2px rgba(27, 25, 22, 0.04), 0 12px 32px rgba(27, 25, 22, 0.04)',
      },
    },
  },
  plugins: [],
};
