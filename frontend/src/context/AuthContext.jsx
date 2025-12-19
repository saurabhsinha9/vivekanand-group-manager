
import { createContext, useContext, useState } from 'react';
import api from '../api/axios';
const AuthContext = createContext();
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => { const t = localStorage.getItem('token'); return t ? { token:t, role:localStorage.getItem('role'), username:localStorage.getItem('username') } : null; });
  const login = async (username, password) => { const { data } = await api.post('/auth/login', { username, password }); localStorage.setItem('token', data.token); localStorage.setItem('role', data.role); localStorage.setItem('username', data.username); setUser({ token:data.token, role:data.role, username:data.username }); };
  const logout = () => { localStorage.clear(); setUser(null); };
  return (<AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>);
};
export const useAuth = () => useContext(AuthContext);
