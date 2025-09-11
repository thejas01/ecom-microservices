import apiClient from './apiClient';

export interface CartItem {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  imageUrl: string;
}

export interface Cart {
  userId: string;
  items: CartItem[];
  totalAmount: number;
  totalItems: number;
}

export interface AddToCartRequest {
  productId: string;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export const cartService = {
  getCart: async (): Promise<Cart> => {
    const response = await apiClient.get('/cart');
    return response.data;
  },

  addToCart: async (request: AddToCartRequest): Promise<Cart> => {
    const response = await apiClient.post('/cart/items', request);
    return response.data;
  },

  updateCartItem: async (productId: string, request: UpdateCartItemRequest): Promise<Cart> => {
    const response = await apiClient.put(`/cart/items/${productId}`, request);
    return response.data;
  },

  removeFromCart: async (productId: string): Promise<Cart> => {
    const response = await apiClient.delete(`/cart/items/${productId}`);
    return response.data;
  },

  clearCart: async (): Promise<void> => {
    await apiClient.delete('/cart');
  },

  // Sync local cart with server after login
  syncCart: async (localCart: CartItem[]): Promise<Cart> => {
    const response = await apiClient.post('/cart/sync', { items: localCart });
    return response.data;
  }
};