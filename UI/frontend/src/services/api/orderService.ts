import apiClient from './apiClient';

export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface ShippingAddress {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface Order {
  id: string;
  userId: string;
  items: OrderItem[];
  shippingAddress: ShippingAddress;
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PAYPAL' | 'CASH_ON_DELIVERY';
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderRequest {
  items: Array<{
    productId: string;
    quantity: number;
  }>;
  shippingAddress: ShippingAddress;
  paymentMethod: string;
}

export interface OrderSearchParams {
  status?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const orderService = {
  createOrder: async (request: CreateOrderRequest): Promise<Order> => {
    const response = await apiClient.post('/orders', request);
    return response.data;
  },

  getAllOrders: async (params?: OrderSearchParams): Promise<Order[]> => {
    const response = await apiClient.get('/orders', { params });
    return response.data;
  },

  getOrderById: async (orderId: string): Promise<Order> => {
    const response = await apiClient.get(`/orders/${orderId}`);
    return response.data;
  },

  updateOrderStatus: async (orderId: string, status: string): Promise<Order> => {
    const response = await apiClient.put(`/orders/${orderId}/status`, { status });
    return response.data;
  },

  cancelOrder: async (orderId: string, reason: string): Promise<Order> => {
    const response = await apiClient.post(`/orders/${orderId}/cancel`, { reason });
    return response.data;
  },

  // Admin endpoints
  getAllOrdersAdmin: async (params?: OrderSearchParams): Promise<Order[]> => {
    const response = await apiClient.get('/orders/admin/all', { params });
    return response.data;
  },

  getOrdersByUserId: async (userId: string): Promise<Order[]> => {
    const response = await apiClient.get(`/orders/user/${userId}`);
    return response.data;
  }
};