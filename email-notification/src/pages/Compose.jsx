import { Form, Input, Button } from "antd";
import { Typography, Box, Paper } from "@mui/material";
import { useCompose } from "../hooks/useCompose";

export const Compose = () => {
    const [form] = Form.useForm();
    const { sendEmail, loading } = useCompose();

    const onFinish = async (values) => {
        const success = await sendEmail(values);
        if (success) form.resetFields();
    };

    return (
        <Box sx={{ maxWidth: 800, margin: '0 auto', mt: 2 }}>
            <Typography variant="h4" fontWeight="bold" mb={3}>Compose Email</Typography>
            <Paper elevation={0} sx={{ p: 4, border: '1px solid #e5e7eb', borderRadius: 2 }}>
                <Form form={form} layout="vertical" onFinish={onFinish} size="large">
                    <Form.Item name="to" label={<Typography fontWeight={500}>Recipient</Typography>} rules={[{ required: true, type: 'email' }]}>
                        <Input placeholder="user@example.com" />
                    </Form.Item>
                    <Form.Item name="subject" label={<Typography fontWeight={500}>Subject</Typography>} rules={[{ required: true }]}>
                        <Input placeholder="What is this email about?" />
                    </Form.Item>
                    <Form.Item name="body" label={<Typography fontWeight={500}>Message Body</Typography>} rules={[{ required: true }]}>
                        <Input.TextArea rows={8} placeholder="Type your message here..." />
                    </Form.Item>
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 4 }}>
                        <Button type="primary" htmlType="submit" loading={loading} style={{ width: '150px' }}>Send Email</Button>
                    </Box>
                </Form>
            </Paper>
        </Box>
    );
};