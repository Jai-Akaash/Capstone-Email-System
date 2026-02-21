import { useState } from "react";
import { Typography, Box, Grid, Card, CardContent, Divider, Paper } from "@mui/material";
import { Input, Select, Modal, Spin } from "antd";
import { SearchLg } from "@untitledui/icons";
import { useMails } from "../hooks/useMails";
import { getStatusChip } from "../utils/statusFormatter";

export const AllMails = () => {
    const [searchTerm, setSearchTerm] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [selectedMail, setSelectedMail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Business logic extracted to hook!
    const { mails, loading } = useMails(searchTerm, statusFilter);

    const openDetail = (mail) => {
        setSelectedMail(mail);
        setIsModalOpen(true);
    };

    return (
        <Box sx={{ maxWidth: 1200, margin: '0 auto' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
                <Typography variant="h4" fontWeight="bold">Email History</Typography>
                <Box sx={{ display: 'flex', gap: 2 }}>
                    <Input 
                        placeholder="Search by recipient..." 
                        prefix={<SearchLg size={18} color="#9ca3af" />}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        style={{ width: 250 }} size="large"
                    />
                    <Select
                        defaultValue="ALL" size="large" style={{ width: 180 }}
                        onChange={(value) => setStatusFilter(value)}
                        options={[
                            { value: 'ALL', label: 'All Statuses' },
                            { value: 'DELIVERED', label: 'Delivered' },
                            { value: 'PROVIDER_SUCCESS', label: 'Provider Success' },
                            { value: 'IN_QUEUE', label: 'In Queue' },
                            { value: 'BOUNCED', label: 'Bounced' },
                        ]}
                    />
                </Box>
            </Box>

            {loading ? <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><Spin size="large" /></Box> : 
                <Grid container spacing={3}>
                    {mails.map((mail) => (
                        <Grid item xs={12} sm={6} md={4} key={mail.id}>
                            <Card onClick={() => openDetail(mail)} sx={{ cursor: 'pointer', border: '1px solid #e5e7eb', boxShadow: 'none' }}>
                                <CardContent>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                                        {getStatusChip(mail.status)}
                                        <Typography variant="caption" color="text.secondary">
                                            {mail.sentAt ? new Date(mail.sentAt).toLocaleDateString() : 'N/A'}
                                        </Typography>
                                    </Box>
                                    <Typography variant="subtitle1" fontWeight="bold" noWrap>{mail.subject || "(No Subject)"}</Typography>
                                    <Typography variant="body2" color="text.secondary" noWrap>To: {mail.recipient}</Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                    ))}
                </Grid>
            }

            <Modal title="Email Details" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} width={700}>
                {selectedMail && (
                    <Box sx={{ mt: 2 }}>
                        <Grid container spacing={2} sx={{ mb: 3 }}>
                            <Grid item xs={6}>
                                <Typography variant="caption" color="text.secondary">Recipient</Typography>
                                <Typography variant="body1" fontWeight="500">{selectedMail.recipient}</Typography>
                            </Grid>
                            <Grid item xs={6}>
                                <Typography variant="caption" color="text.secondary">Status</Typography>
                                <Box sx={{ mt: 0.5 }}>{getStatusChip(selectedMail.status)}</Box>
                            </Grid>
                        </Grid>
                        {selectedMail.errorMessage && (
                            <Paper sx={{ p: 2, bgcolor: '#fef2f2', border: '1px solid #f87171', mb: 3 }}>
                                <Typography variant="subtitle2" color="error">Delivery Error:</Typography>
                                <Typography variant="body2" color="error">{selectedMail.errorMessage}</Typography>
                            </Paper>
                        )}
                        <Divider sx={{ my: 2 }} />
                        <Typography variant="subtitle2" color="text.secondary" mb={1}>Message Body</Typography>
                        <Paper elevation={0} sx={{ p: 2, bgcolor: '#f3f4f6', borderRadius: 2, whiteSpace: 'pre-wrap', maxHeight: '300px', overflowY: 'auto' }}>
                            {selectedMail.body}
                        </Paper>
                    </Box>
                )}
            </Modal>
        </Box>
    );
};