
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
        // අර කලින් ආපු Hydration Error එක ආයෙත් එන එක නවත්වන්න suppressHydrationWarning දැම්මා
        <html lang="en" suppressHydrationWarning>
        <body>{children}</body>
        </html>
    );
}