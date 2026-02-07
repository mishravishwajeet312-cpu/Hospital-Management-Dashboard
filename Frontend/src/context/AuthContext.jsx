import { createContext, useContext, useEffect, useMemo, useState } from "react";
import api from "../api/axios";

const AuthContext = createContext(null);

const TOKEN_KEY = "authToken";
const USER_KEY = "authUser";

const normalizeUser = (rawUser) => {
  if (!rawUser) {
    return null;
  }
  const email = rawUser.email;
  const name = rawUser.name || (email ? email.split("@")[0] : undefined);
  return { ...rawUser, name };
};

const readUserFromStorage = () => {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return normalizeUser(JSON.parse(raw));
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
};

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(() => readUserFromStorage());

  useEffect(() => {
    if (token) {
      api.defaults.headers.common.Authorization = `Bearer ${token}`;
    } else {
      delete api.defaults.headers.common.Authorization;
    }
  }, [token]);

  const setAuthState = (authData) => {
    if (authData?.token) {
      localStorage.setItem(TOKEN_KEY, authData.token);
      setToken(authData.token);
    }

    const nextUser = normalizeUser({
      email: authData?.email,
      role: authData?.role,
      name: authData?.name,
    });

    localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    setUser(nextUser);
  };

  const login = async ({ email, password }) => {
    const response = await api.post("/auth/login", { email, password });
    setAuthState(response.data);
    return response.data;
  };

  const register = async ({ name, email, password }) => {
    const response = await api.post("/auth/register", { name, email, password });
    return response.data;
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
  };

  const isAuthenticated = Boolean(token);

  const value = useMemo(
    () => ({ token, user, isAuthenticated, login, logout, register }),
    [token, user, isAuthenticated]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
