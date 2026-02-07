/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}"
  ],
  theme: {
    extend: {
      colors: {
        ink: "#0b1b33",
        brand: "#0ea5a4",
        brandDark: "#0b7d7c",
        surface: "#f4f7fb",
        card: "#ffffff",
        accent: "#f59e0b",
        sidebar: "#0f2a4b",
        sidebarLight: "#16355e"
      },
      fontFamily: {
        sans: ["Manrope", "system-ui", "sans-serif"],
        display: ["Space Grotesk", "Manrope", "sans-serif"]
      },
      boxShadow: {
        lift: "0 18px 40px -26px rgba(15, 23, 42, 0.45)",
        soft: "0 12px 24px -18px rgba(15, 23, 42, 0.35)"
      }
    },
  },
  plugins: [],
};
