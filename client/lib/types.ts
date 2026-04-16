export type ApiResponse<T> = {
  code: string;
  message: string;
  data: T;
};

export type ApiErrorResponse = {
  errorCode: string;
  message: string;
  fieldErrors?: Record<string, string> | null;
};

export type RoleSummary = {
  id: number;
  code: string;
  name: string;
};

export type CurrentUser = {
  id: number;
  username: string;
  fullName: string;
  email: string;
  selectedRoleId: number;
  selectedRoleCode: string;
  roleCodes: string[];
  permissions: string[];
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: CurrentUser;
};

export type Role = {
  id: number;
  code: string;
  name: string;
  description: string;
  status: number;
  systemRole: boolean;
  permissionIds: number[];
  permissionKeys: string[];
};

export type Permission = {
  id: number;
  moduleCode: string;
  moduleName: string;
  resourceCode: string;
  resourceName: string;
  actionCode: string;
  actionName: string;
  status: number;
};

export type User = {
  id: number;
  username: string;
  fullName: string;
  email: string;
  status: number;
  sessionVersion: number;
  roles: RoleSummary[];
};

export type Subscription = {
  id: number;
  title: string;
  description: string;
  status: number;
};

export type PageData<T> = {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
};

export type Tokens = {
  accessToken: string;
  refreshToken: string;
};
