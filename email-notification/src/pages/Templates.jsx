import { useState } from "react";
import { Button, Modal, Form, Input, Popconfirm } from "antd";
import { Card, CardContent, Typography, Grid, IconButton, Box } from "@mui/material";
import { Edit01, Trash01, Send01 } from "@untitledui/icons";
import { useTemplates } from "../hooks/useTemplates";
import { useCompose } from "../hooks/useCompose";

export const Templates = () => {
    // Look how clean this is! Hooks do all the heavy lifting.
    const { templates, saveTemplate, deleteTemplate } = useTemplates();
    const { sendEmail } = useCompose();
    
    const [isCrudModalOpen, setIsCrudModalOpen] = useState(false);
    const [isSendModalOpen, setIsSendModalOpen] = useState(false);
    const [form] = Form.useForm();
    const [sendForm] = Form.useForm();
    const [editingId, setEditingId] = useState(null);
    const [selectedTemplate, setSelectedTemplate] = useState(null);

    const openCrudModal = (template = null) => {
        setEditingId(template?.id || null);
        template ? form.setFieldsValue(template) : form.resetFields();
        setIsCrudModalOpen(true);
    };

    const handleSave = async (values) => {
        const success = await saveTemplate(editingId, values);
        if (success) setIsCrudModalOpen(false);
    };

    const openSendModal = (template) => {
        setSelectedTemplate(template);
        sendForm.setFieldsValue({ subject: template.subject, body: template.body });
        setIsSendModalOpen(true);
    };

    const handleSend = async (values) => {
        const success = await sendEmail(values);
        if (success) setIsSendModalOpen(false);
    };

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <Typography variant="h4" fontWeight="bold">Email Templates</Typography>
                <Button type="primary" size="large" onClick={() => openCrudModal()}>+ Create Template</Button>
            </div>

            <Grid container spacing={3}>
                {templates.map(template => (
                    <Grid item xs={12} sm={6} md={4} key={template.id}>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', border: '1px solid #e5e7eb', boxShadow: 'none' }}>
                            <CardContent sx={{ flexGrow: 1 }}>
                                <Typography variant="h6" fontWeight="bold">{template.name}</Typography>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>{template.subject}</Typography>
                                <Typography variant="body2" sx={{ display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                                    {template.body}
                                </Typography>
                            </CardContent>
                            <Box sx={{ p: 2, pt: 0, display: 'flex', justifyContent: 'space-between' }}>
                                <Button type="default" icon={<Send01 />} onClick={() => openSendModal(template)}>Use</Button>
                                <div>
                                    <IconButton size="small" onClick={() => openCrudModal(template)} color="primary"><Edit01 /></IconButton>
                                    <Popconfirm title="Delete?" onConfirm={() => deleteTemplate(template.id)}>
                                        <IconButton size="small" color="error"><Trash01 /></IconButton>
                                    </Popconfirm>
                                </div>
                            </Box>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            <Modal title={editingId ? "Edit" : "Create"} open={isCrudModalOpen} onCancel={() => setIsCrudModalOpen(false)} onOk={() => form.submit()}>
                <Form form={form} layout="vertical" onFinish={handleSave}>
                    <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
                    <Form.Item name="subject" label="Subject" rules={[{ required: true }]}><Input /></Form.Item>
                    <Form.Item name="body" label="Body" rules={[{ required: true }]}><Input.TextArea rows={4} /></Form.Item>
                </Form>
            </Modal>

            <Modal title={`Send: ${selectedTemplate?.name}`} open={isSendModalOpen} onCancel={() => setIsSendModalOpen(false)} onOk={() => sendForm.submit()}>
                <Form form={sendForm} layout="vertical" onFinish={handleSend}>
                    <Form.Item name="to" label="To" rules={[{ required: true, type: 'email' }]}><Input /></Form.Item>
                    <Form.Item name="subject" label="Subject"><Input /></Form.Item>
                    <Form.Item name="body" label="Body"><Input.TextArea rows={4} /></Form.Item>
                </Form>
            </Modal>
        </div>
    );
};