import "@/app/globals.css";

export const metadata = {
    title: "MyOpty Application",
    description: "Epic 8 - Dealer Workflow",
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