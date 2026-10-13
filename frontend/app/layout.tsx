import React from "react";
// @ts-ignore
import "./globals.css";

export const metadata = {
    title: "MyOpty Application",
    description: "Epic B - Dealer Workflow",
};

export default function RootLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <html lang="en" suppressHydrationWarning={true}>
        <body>{children}</body>
        </html>
    );
}