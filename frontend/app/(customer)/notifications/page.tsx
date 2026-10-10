import NotificationList from "@/features/order/components/NotificationList";

/**
 * The customer's notifications: every message the shop has sent about their
 * orders, newest first. A thin server component; the list is the whole feature.
 */
export default function NotificationsPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Notifications</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Updates about your orders, most recent first.
        </p>
      </div>
      <NotificationList emptyText="No notifications yet." />
    </div>
  );
}
