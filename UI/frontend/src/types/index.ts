// Re-export all types from services
export type { 
  LoginRequest, 
  RegisterRequest, 
  AuthResponse 
} from '../services/api/authService';

export type { 
  Product, 
  Category, 
  ProductSearchParams, 
  PageableResponse 
} from '../services/api/productService';

export type { 
  CartItem, 
  Cart, 
  AddToCartRequest, 
  UpdateCartItemRequest 
} from '../services/api/cartService';

export type { 
  OrderItem, 
  ShippingAddress, 
  Order, 
  CreateOrderRequest, 
  OrderSearchParams 
} from '../services/api/orderService';

// Common types
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

// UI Types
export interface SelectOption {
  value: string;
  label: string;
}

export interface BreadcrumbItem {
  label: string;
  href?: string;
}

// Form validation types
export interface ValidationError {
  field: string;
  message: string;
}