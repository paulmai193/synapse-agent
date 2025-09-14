import { useSelector } from 'react-redux';
import { RootState } from '../store';

export const useAuth = () => {
  const auth = useSelector((state: RootState) => state.auth);
  
  const hasRole = (roleName: string) => {
    return auth.user?.roles.some(role => role.name === roleName) || false;
  };

  const hasAnyRole = (roleNames: string[]) => {
    return auth.user?.roles.some(role => roleNames.includes(role.name)) || false;
  };

  const isAdmin = () => {
    return hasAnyRole(['SYSTEM_ADMIN', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN']);
  };

  return {
    ...auth,
    hasRole,
    hasAnyRole,
    isAdmin,
  };
};