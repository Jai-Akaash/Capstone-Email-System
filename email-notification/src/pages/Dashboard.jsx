import { Typography, Box, Grid, Paper } from "@mui/material";
import { Table, Spin } from "antd";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { CheckDone01, AlertCircle, ClockFastForward, BarChartSquare02 } from "@untitledui/icons";
import { useDashboard } from "../hooks/useDashboard";
import { getStatusTag } from "../utils/statusFormatter";

export const Dashboard = () => {
    const { stats, recentLogs, loading } = useDashboard();

    const columns = [
        { title: 'Log ID', dataIndex: 'id', key: 'id', width: 80 },
        { title: 'Recipient', dataIndex: 'recipient', key: 'recipient' },
        { title: 'Subject', dataIndex: 'subject', key: 'subject', ellipsis: true },
        { title: 'Status', dataIndex: 'status', key: 'status', render: (status) => getStatusTag(status) },
        { title: 'Sent At', dataIndex: 'sentAt', key: 'sentAt', render: (date) => date ? new Date(date).toLocaleString() : "Pending" },
    ];

    const chartData = stats?.dailyActivity?.map(item => ({
        date: item[0], emails: item[1]
    })).reverse() || [];

    if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><Spin size="large" tip="Loading Analytics..." /></Box>;

    return (
        <Box sx={{ maxWidth: 1200, margin: '0 auto' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
                <BarChartSquare02 size={32} color="#4f46e5" />
                <Typography variant="h4" fontWeight="bold">System Analytics</Typography>
            </Box>

            <Grid container spacing={3} sx={{ mb: 4 }}>
                <Grid item xs={12} sm={6} md={3}>
                    <Paper elevation={0} sx={{ p: 3, border: '1px solid #e5e7eb', borderRadius: 2, borderTop: '4px solid #4f46e5' }}>
                        <Typography variant="subtitle2" color="text.secondary">Total Volume</Typography>
                        <Typography variant="h3" fontWeight="bold" sx={{ mt: 1 }}>{stats?.totalEmails || 0}</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Paper elevation={0} sx={{ p: 3, border: '1px solid #e5e7eb', borderRadius: 2, borderTop: '4px solid #10b981' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="subtitle2" color="text.secondary">Delivery Rate</Typography>
                            <CheckDone01 color="#10b981" />
                        </Box>
                        <Typography variant="h3" fontWeight="bold" sx={{ mt: 1 }}>{stats?.deliveryRate || "0.00%"}</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Paper elevation={0} sx={{ p: 3, border: '1px solid #e5e7eb', borderRadius: 2, borderTop: '4px solid #ef4444' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="subtitle2" color="text.secondary">Bounced / Dropped</Typography>
                            <AlertCircle color="#ef4444" />
                        </Box>
                        <Typography variant="h3" fontWeight="bold" sx={{ mt: 1 }}>{stats?.bouncedEmails || 0}</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Paper elevation={0} sx={{ p: 3, border: '1px solid #e5e7eb', borderRadius: 2, borderTop: '4px solid #f59e0b' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="subtitle2" color="text.secondary">Currently Queued</Typography>
                            <ClockFastForward color="#f59e0b" />
                        </Box>
                        <Typography variant="h3" fontWeight="bold" sx={{ mt: 1 }}>{stats?.queuedEmails || 0}</Typography>
                    </Paper>
                </Grid>
            </Grid>

            <Paper elevation={0} sx={{ p: 3, border: '1px solid #e5e7eb', borderRadius: 2, mb: 4 }}>
                <Typography variant="h6" fontWeight="bold" mb={3}>7-Day Dispatch Volume</Typography>
                <Box sx={{ height: 300, width: '100%' }}>
                    {chartData.length > 0 ? (
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                                <XAxis dataKey="date" axisLine={false} tickLine={false} />
                                <YAxis axisLine={false} tickLine={false} />
                                <Tooltip />
                                <Line type="monotone" dataKey="emails" stroke="#4f46e5" strokeWidth={3} />
                            </LineChart>
                        </ResponsiveContainer>
                    ) : (
                        <Box sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>No chart data.</Box>
                    )}
                </Box>
            </Paper>

            <Paper elevation={0} sx={{ p: 3, border: '1px solid #e5e7eb', borderRadius: 2 }}>
                <Typography variant="h6" fontWeight="bold" mb={3}>Live Activity Feed</Typography>
                <Table dataSource={recentLogs} columns={columns} rowKey="id" pagination={false} size="middle" />
            </Paper>
        </Box>
    );
};