import { useState, useEffect } from "react";
import { dealerApi } from "@/features/workflow/api";

interface DealerEmailStatus {
  emailId: number;
  dealerId: number;
  dealerName: string;
  subject: string;
  status: "DRAFT" | "SENT" | "REPLIED" | "FULFILLED";
  createdAt: string;
  sentAt: string | null;
}

interface SentEmailsDashboard {
  emails: DealerEmailStatus[];
  totalEmails: number;
  statusCounts: {
    DRAFT: number;
    SENT: number;
    REPLIED: number;
    FULFILLED: number;
  };
}

export default function SentEmailsPage() {
  const [data, setData] = useState<SentEmailsDashboard>({
    emails: [],
    totalEmails: 0,
    statusCounts: {
      DRAFT: 0,
      SENT: 0,
      REPLIED: 0,
      FULFILLED: 0,
    },
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        // Fetch sent emails - in a real implementation, this would be a dedicated API endpoint
        // For now, we'll use the dealer API to get active dealers and simulate email status
        const dealerResult = await dealerApi.getActiveDealers();
        
        if (dealerResult.success && dealerResult.data) {
          const emails: DealerEmailStatus[] = dealerResult.data.map((dealer: any) => ({
            emailId: Math.floor(Math.random() * 1000),
            dealerId: dealer.dealerId,
            dealerName: dealer.name,
            subject: `Stock Request - ${dealer.name}`,
            status: Math.random() > 0.5 ? "SENT" : "DRAFT",
            createdAt: new Date().toISOString(),
            sentAt: Math.random() > 0.5 ? new Date().toISOString() : null,
          }));
          
          const statusCounts = {
            DRAFT: 0,
            SENT: 0,
            REPLIED: 0,
            FULFILLED: 0,
          };
          
          emails.forEach((email) => {
            statusCounts[email.status as keyof typeof statusCounts]!(
              statusCounts[email.status as keyof typeof statusCounts]! + 1
            );
          });
          
          setData({
            emails,
            totalEmails: emails.length,
            statusCounts,
          });
        }
      } catch (err: any) {
        setError(err.message || "Failed to load sent emails");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  if (loading) {
    return <div className="min-h-80 flex items-center justify-center">Loading...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-600">Error: {error}</div>;
  }

  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-4">Sent Emails & Status</h2>
      
      <div className="grid grid-cols-2 gap-2 mb-4">
        <div className="bg-gray-50 rounded p-3">
          <p className="text-xs font-medium text-gray-500">DRAFT</p>
          <p className="text-2xl font-bold text-gray-800">{data.statusCounts.DRAFT}</p>
        </div>
        <div className="bg-gray-50 rounded p-3">
          <p className="text-xs font-medium text-gray-500">SENT</p>
          <p className="text-2xl font-bold text-green-600">{data.statusCounts.SENT}</p>
        </div>
        <div className="bg-gray-50 rounded p-3">
          <p className="text-xs font-medium text-gray-500">REPLIED</p>
          <p className="text-2xl font-bold text-orange-600">{data.statusCounts.REPLIED}</p>
        </div>
        <div className="bg-gray-50 rounded p-3">
          <p className="text-xs font-medium text-gray-500">FULFILLED</p>
          <p className="text-2xl font-bold text-blue-600">{data.statusCounts.FULFILLED}</p>
        </div>
      </div>
      
      <div className="bg-white rounded p-4 shadow-sm mb-6">
        <h3 className="text-lg font-medium mb-3">Email History</h3>
        <div className="overflow-x-auto">
          <table className="min-w-full rounded border">
            <thead>
              <tr className="bg-gray-50">
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Dealer</th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Subject</th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sent</th>
              </thead>
              <tbody>
                {data.emails.map((email) => (
                  <tr key={email.emailId} className="border-b">
                    <td className="p-3">{email.dealerName}</td>
                    <td className="p-3">{email.subject}</td>
                    <td className="p-3">
                      <span className="text-xs font-medium px-2 py-1 rounded {
                        email.status === "DRAFT"
                          ? "bg-gray-100 text-gray-700"
                          : email.status === "SENT"
                            ? "bg-green-100 text-green-800"
                            : email.status === "REPLIED"
                              ? "bg-orange-100 text-orange-800"
                              : "bg-blue-100 text-blue-800"
                      }">
                        {email.status}
                      </span>
                    </td>
                    <td className="p-3">{email.sentAt ? new Date(email.sentAt).toLocaleDateString() : "Not sent"}</td>
                  </tr>
                ))}
                {data.emails.length === 0 && (
                  <tr>
                    <td colSpan="4" className="p-4 text-center text-gray-500">
                      No emails found
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}