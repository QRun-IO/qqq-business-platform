/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './lib/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        background: '#fbfaf6',
        foreground: '#17201b',
        primary: { DEFAULT: '#173f35', foreground: '#ffffff' },
        secondary: { DEFAULT: '#dfe7dc', foreground: '#173f35' },
        destructive: { DEFAULT: '#b42318', foreground: '#ffffff' },
        accent: { DEFAULT: '#edf0e9', foreground: '#173f35' },
        input: 'rgba(23,32,27,.16)',
        ring: '#426c5e',
      },
    },
  },
  plugins: [require('@tailwindcss/forms'), require('@tailwindcss/typography')],
}
