import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Collapse,
} from '@mui/material';
import {
  Dashboard,
  Search,
  QuestionAnswer,
  Upload,
  People,
  Business,
  Assignment,
  Analytics,
  Settings,
  ExpandLess,
  ExpandMore,
} from '@mui/icons-material';
import { useSelector } from 'react-redux';
import { useNavigate, useLocation } from 'react-router-dom';
import { RootState } from '../../store';
import { useAuth } from '../../hooks/useAuth';

interface MenuItem {
  text: string;
  icon: React.ReactElement;
  path?: string;
  roles?: string[];
  children?: MenuItem[];
}

const menuItems: MenuItem[] = [
  {
    text: 'Dashboard',
    icon: <Dashboard />,
    path: '/dashboard',
  },
  {
    text: 'Search',
    icon: <Search />,
    path: '/search',
  },
  {
    text: 'Q&A',
    icon: <QuestionAnswer />,
    path: '/qa',
  },
  {
    text: 'Documents',
    icon: <Upload />,
    path: '/documents',
  },
  {
    text: 'Administration',
    icon: <Settings />,
    roles: ['SYSTEM_ADMIN', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN'],
    children: [
      {
        text: 'Users',
        icon: <People />,
        path: '/admin/users',
        roles: ['SYSTEM_ADMIN'],
      },
      {
        text: 'Projects',
        icon: <Assignment />,
        path: '/admin/projects',
        roles: ['SYSTEM_ADMIN', 'PROJECT_ADMIN'],
      },
      {
        text: 'Departments',
        icon: <Business />,
        path: '/admin/departments',
        roles: ['SYSTEM_ADMIN', 'DEPARTMENT_ADMIN'],
      },
      {
        text: 'Analytics',
        icon: <Analytics />,
        path: '/admin/analytics',
        roles: ['SYSTEM_ADMIN'],
      },
    ],
  },
];

const Sidebar: React.FC = () => {
  const { sidebarOpen } = useSelector((state: RootState) => state.ui);
  const { hasAnyRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [openItems, setOpenItems] = React.useState<string[]>([]);

  const handleItemClick = (item: MenuItem) => {
    if (item.path) {
      navigate(item.path);
    } else if (item.children) {
      const isOpen = openItems.includes(item.text);
      setOpenItems(isOpen 
        ? openItems.filter(text => text !== item.text)
        : [...openItems, item.text]
      );
    }
  };

  const isItemVisible = (item: MenuItem): boolean => {
    if (!item.roles) return true;
    return hasAnyRole(item.roles);
  };

  const isItemActive = (path?: string): boolean => {
    if (!path) return false;
    return location.pathname === path;
  };

  const renderMenuItem = (item: MenuItem, level = 0) => {
    if (!isItemVisible(item)) return null;

    const hasChildren = item.children && item.children.length > 0;
    const isOpen = openItems.includes(item.text);
    const isActive = isItemActive(item.path);

    return (
      <React.Fragment key={item.text}>
        <ListItem disablePadding sx={{ display: 'block' }}>
          <ListItemButton
            onClick={() => handleItemClick(item)}
            selected={isActive}
            sx={{
              minHeight: 48,
              justifyContent: sidebarOpen ? 'initial' : 'center',
              px: 2.5,
              pl: level > 0 ? 4 : 2.5,
            }}
          >
            <ListItemIcon
              sx={{
                minWidth: 0,
                mr: sidebarOpen ? 3 : 'auto',
                justifyContent: 'center',
              }}
            >
              {item.icon}
            </ListItemIcon>
            <ListItemText
              primary={item.text}
              sx={{ opacity: sidebarOpen ? 1 : 0 }}
            />
            {hasChildren && sidebarOpen && (
              isOpen ? <ExpandLess /> : <ExpandMore />
            )}
          </ListItemButton>
        </ListItem>
        
        {hasChildren && (
          <Collapse in={isOpen && sidebarOpen} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {item.children!.map(child => renderMenuItem(child, level + 1))}
            </List>
          </Collapse>
        )}
      </React.Fragment>
    );
  };

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: sidebarOpen ? 240 : 60,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: sidebarOpen ? 240 : 60,
          boxSizing: 'border-box',
          transition: 'width 0.3s',
          overflowX: 'hidden',
        },
      }}
    >
      <div style={{ height: 64 }} /> {/* Spacer for header */}
      <Divider />
      <List>
        {menuItems.map(item => renderMenuItem(item))}
      </List>
    </Drawer>
  );
};

export default Sidebar;