import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "@/components/Providers";

export const metadata: Metadata = {
  title: "SynFlow | Enterprise Intelligence",
  description: "Private network intelligence and deal matching platform",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="bg-white text-text-primary">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
