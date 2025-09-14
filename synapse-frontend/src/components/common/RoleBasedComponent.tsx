import React from 'react';
import { useAuth } from '../../hooks/useAuth';

interface RoleBasedComponentProps {
  children: React.ReactNode;
  requiredRoles?: string[];
  fallback?: React.ReactNode;
}

const RoleBasedComponent: React.FC<RoleBasedComponentProps> = ({
  children,
  requiredRoles = [],
  fallback = null
}) => {
  const { hasAnyRole } = useAuth();

  if (requiredRoles.length === 0) {
    return <>{children}</>;
  }

  if (hasAnyRole(requiredRoles)) {
    return <>{children}</>;
  }

  return <>{fallback}</>;
};

export default RoleBasedComponent;