import api from "./api";

interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export const login = async (data: LoginRequest) => {
  const response = await api.post<string>(
    "/api/users/login",
    data
  );

  return response.data;
};

export const register = async (
  data: RegisterRequest
) => {
  const response = await api.post(
    "/api/users/register",
    {
      ...data,
      role: "CUSTOMER",
    }
  );

  return response.data;
};

export const getCurrentUser = async () => {
  const response = await api.get("/api/users/me");

  return response.data;
};