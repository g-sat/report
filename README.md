# 🚀 Inventory Management System

> **Professional Inventory Management with PDF Report Generation**

[![React](https://img.shields.io/badge/React-19.1.1-blue.svg)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-orange.svg)](https://www.mysql.com/)
[![JasperReports](https://img.shields.io/badge/JasperReports-6.21.3-red.svg)](https://community.jaspersoft.com/project/jasperreports-library)

---

## ✨ Features

- 📊 **Real-time Inventory Management** - Add, edit, and remove items instantly
- 💾 **MySQL Database Integration** - Persistent data storage with ACID compliance
- 📄 **Professional PDF Reports** - Generate beautiful reports using JasperReports
- 🎨 **Modern React UI** - Clean, responsive interface with professional styling
- ⚡ **Spring Boot Backend** - Enterprise-grade REST API with JPA/Hibernate
- 🔄 **Real-time Updates** - Automatic synchronization between frontend and database
- 📱 **Mobile Responsive** - Works perfectly on all devices

---

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React App     │    │  Spring Boot    │    │   MySQL         │
│   (Frontend)    │◄──►│   (Backend)     │◄──►│   Database      │
│   Port: 5174    │    │   Port: 8080    │    │   Port: 3306    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 🛠️ Prerequisites

Before you begin, ensure you have the following installed:

### Required Software
- **Java 17+** - [Download OpenJDK](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
- **Node.js 18+** - [Download Node.js](https://nodejs.org/)
- **MySQL 8.0+** - [Download MySQL](https://dev.mysql.com/downloads/mysql/)
- **Maven 3.9+** - [Download Maven](https://maven.apache.org/download.cgi)

### Verify Installation
```bash
java -version          # Should show Java 17+
node --version         # Should show Node 18+
npm --version          # Should show npm version
mysql --version        # Should show MySQL 8.0+
mvn -version           # Should show Maven 3.9+
```

---

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <your-repository-url>
cd report
```

### 2. Database Setup
```bash
# Start MySQL service
net start mysql

# Connect to MySQL and set password (if needed)
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY '2221';
FLUSH PRIVILEGES;
exit;
```

### 3. Backend Configuration
```bash
cd backend
```

**Update Database Password** in `src/main/resources/application.properties`:
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 4. Start Backend Server
```bash
# Using Maven Wrapper (recommended)
.\mvnw.cmd spring-boot:run

# OR using system Maven
mvn spring-boot:run
```

**Expected Output:**
```
:: Spring Boot ::                (v3.3.2)
Tomcat started on port 8080
Started ReportApplication in X seconds
```

### 5. Start Frontend Server
```bash
# Open new terminal
cd ..  # Back to project root
npm install
npm run dev
```

**Expected Output:**
```
VITE v7.1.2  ready in XXX ms
➜  Local:   http://localhost:5174/
```

### 6. Access Application
🌐 **Open your browser and navigate to:** `http://localhost:5174`

---

## 📁 Project Structure

```
report/
├── 📁 backend/                          # Spring Boot Backend
│   ├── 📁 src/main/java/
│   │   └── 📁 com/example/report/
│   │       ├── 📄 ReportApplication.java    # Main Spring Boot App
│   │       ├── 📁 controller/               # REST API Controllers
│   │       ├── 📁 service/                  # Business Logic
│   │       ├── 📁 repository/               # Data Access Layer
│   │       └── 📁 model/                    # JPA Entities
│   ├── 📁 src/main/resources/
│   │   ├── 📄 application.properties        # Database Configuration
│   │   └── 📁 reports/                      # JasperReports Templates
│   └── 📄 pom.xml                           # Maven Dependencies
├── 📁 src/                                 # React Frontend
│   ├── 📄 App.tsx                          # Main React Component
│   ├── 📄 App.css                          # Styling
│   └── 📄 main.tsx                         # React Entry Point
├── 📄 package.json                         # Node.js Dependencies
├── 📄 vite.config.ts                       # Vite Configuration
└── 📄 README.md                            # This File
```

---

## ⚙️ Configuration

### Database Configuration
**File:** `backend/src/main/resources/application.properties`

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_db
spring.datasource.username=root
spring.datasource.password=2221
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Server Configuration
server.port=8080
```

### Frontend Configuration
**File:** `vite.config.ts`

```typescript
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',  // Backend API proxy
    },
  },
})
```

---

## 🔧 Customization

### Adding New Fields to Items
1. **Update Entity** (`backend/src/main/java/com/example/report/model/Item.java`)
2. **Update Database** (automatic with `hibernate.ddl-auto=update`)
3. **Update Frontend** (`src/App.tsx`)

### Customizing PDF Reports
1. **Modify Template** (`backend/src/main/resources/reports/sample_report.jrxml`)
2. **Update Controller** for new parameters
3. **Restart Backend** to apply changes

### Styling Changes
- **CSS File:** `src/App.css`
- **Hot Reload:** Changes apply instantly in development

---

## 🧪 Testing

### Backend Testing
```bash
cd backend
mvn test
```

### Frontend Testing
```bash
npm test
```

### Manual Testing
1. **Add Items** - Test item creation
2. **Remove Items** - Test item deletion
3. **Generate Reports** - Test PDF generation
4. **Database Persistence** - Verify data is saved

---

## 🚨 Troubleshooting

### Common Issues

#### Backend Won't Start
```bash
# Check Java version
java -version

# Check MySQL connection
mysql -u root -p

# Check port availability
netstat -an | findstr 8080
```

#### Frontend Can't Connect to Backend
```bash
# Verify backend is running
curl http://localhost:8080/api/items

# Check Vite proxy configuration
# Ensure backend is on port 8080
```

#### Database Connection Issues
```bash
# Verify MySQL service is running
net start mysql

# Check credentials in application.properties
# Test connection manually
mysql -u root -p
```

#### Port Already in Use
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

---

## 📚 API Documentation

### Items Management
```http
GET    /api/items          # Get all items
POST   /api/items          # Create new item
PUT    /api/items/{id}     # Update item
DELETE /api/items/{id}     # Delete item
```

### Report Generation
```http
GET  /api/reports/generate     # Generate PDF from database
POST /api/reports/generate     # Generate PDF from provided data
```

### Example API Calls
```bash
# Get all items
curl http://localhost:8080/api/items

# Add new item
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Item","quantity":5,"price":10.99}'

# Generate PDF report
curl http://localhost:8080/api/reports/generate \
  --output inventory-report.pdf
```

---

## 🚀 Deployment

### Development
```bash
# Backend
cd backend && mvn spring-boot:run

# Frontend
npm run dev
```

### Production
```bash
# Build frontend
npm run build

# Build backend
cd backend && mvn clean package

# Run backend JAR
java -jar target/report-backend-0.0.1-SNAPSHOT.jar
```

