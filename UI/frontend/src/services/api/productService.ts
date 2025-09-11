import apiClient from './apiClient';

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  categoryId: string;
  categoryName: string;
  imageUrl: string;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: string;
  name: string;
  description: string;
}

export interface ProductSearchParams {
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface PageableResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export const productService = {
  getAllProducts: async (params?: ProductSearchParams): Promise<PageableResponse<Product>> => {
    const response = await apiClient.get('/products', { params });
    return response.data;
  },

  getProductById: async (id: string): Promise<Product> => {
    const response = await apiClient.get(`/products/${id}`);
    return response.data;
  },

  searchProducts: async (keyword: string): Promise<Product[]> => {
    const response = await apiClient.get('/products/search', { params: { keyword } });
    return response.data;
  },

  getProductsByCategory: async (categoryId: string): Promise<Product[]> => {
    const response = await apiClient.get(`/products/category/${categoryId}`);
    return response.data;
  },

  getAllCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get('/categories');
    return response.data;
  },

  getCategoryById: async (id: string): Promise<Category> => {
    const response = await apiClient.get(`/categories/${id}`);
    return response.data;
  },

  // Admin endpoints
  createProduct: async (product: Omit<Product, 'id' | 'createdAt' | 'updatedAt'>): Promise<Product> => {
    const response = await apiClient.post('/products', product);
    return response.data;
  },

  updateProduct: async (id: string, product: Partial<Product>): Promise<Product> => {
    const response = await apiClient.put(`/products/${id}`, product);
    return response.data;
  },

  deleteProduct: async (id: string): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },

  createCategory: async (category: Omit<Category, 'id'>): Promise<Category> => {
    const response = await apiClient.post('/categories', category);
    return response.data;
  },

  updateCategory: async (id: string, category: Partial<Category>): Promise<Category> => {
    const response = await apiClient.put(`/categories/${id}`, category);
    return response.data;
  },

  deleteCategory: async (id: string): Promise<void> => {
    await apiClient.delete(`/categories/${id}`);
  }
};