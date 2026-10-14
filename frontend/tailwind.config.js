
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        eco: {
          50: '#eff8f0',
          100: '#dcefe0',
          500: '#398653',
          600: '#287143',
          700: '#205a38',
          900: '#173c2a'
        }
      },
      boxShadow: {
        soft: '0 8px 30px rgba(28, 65, 42, 0.06)'
      }
    }
  },
  plugins: []
};
