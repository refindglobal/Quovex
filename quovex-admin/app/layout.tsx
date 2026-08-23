import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'Quovex Admin — Content Studio & Demand Intelligence',
  description: 'Internal authoring and editorial control plane for Quovex Originals',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="bg-background text-foreground antialiased min-h-screen">
        {children}
      </body>
    </html>
  );
}
