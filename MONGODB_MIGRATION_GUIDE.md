# MongoDB Atlas Migration Guide for Axiom

Your Axiom application has been successfully migrated from PostgreSQL to MongoDB! Follow these steps to complete the setup.

## 1. Create MongoDB Atlas Cluster

### Step 1: Sign up or Log in to MongoDB Atlas
- Visit [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
- Create a free account or log in to your existing account

### Step 2: Create a New Project
- Click "New Project"
- Enter "Axiom" as the project name
- Click "Create Project"

### Step 3: Create a Cluster
- Click "Create" under "Deployments"
- Select **M0 (Free)** tier for development
- Choose your preferred region (recommended: closest to your location)
- Cluster name: "axiom-cluster"
- Click "Create Deployment"

### Step 4: Wait for Cluster Setup
- The cluster will take a few minutes to deploy
- You'll receive an email confirmation once it's ready

## 2. Set Up Database Access

### Step 1: Create Database User
- In the MongoDB Atlas dashboard, go to "Database Access"
- Click "Add New Database User"
- **Username**: axiom_user
- **Password**: (Generate a strong password - save this!)
- **Built-in Role**: Read and write to any database
- Click "Add User"

### Step 2: Configure Network Access
- Go to "Network Access" in the left sidebar
- Click "Add IP Address"
- Click "Allow Access from Anywhere" (for development) or enter your IP
- Click "Confirm"

## 3. Get Connection String

### Step 1: Get Connection URI
- Go to "Clusters" and click "Connect"
- Click "Drivers" 
- Select "Java" driver version 4.9 or higher
- Copy the connection string that looks like:
  ```
  mongodb+srv://axiom_user:<password>@axiom-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
  ```
- Replace `<password>` with the password you created in Database Access
- Note: Do NOT include angle brackets in the actual URL

## 4. Configure Environment Variables

### Option A: Using Environment Variables (Recommended for Production)
```bash
export MONGODB_ATLAS_URI="mongodb+srv://axiom_user:YOUR_PASSWORD@axiom-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority"
export JWT_SECRET="your-secret-key-here"
```

### Option B: Update application.properties (For Development Only)
Edit `src/main/resources/application.properties`:
```properties
spring.application.name=Axiom
spring.data.mongodb.uri=mongodb+srv://axiom_user:YOUR_PASSWORD@axiom-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
spring.data.mongodb.database=axiom
security.jwt.secret=your-secret-key-here
security.jwt.expiration-ms=3600000
```

## 5. Build and Test the Application

### Step 1: Clean Build
```bash
mvn clean install
```

### Step 2: Run the Application
```bash
mvn spring-boot:run
```

### Step 3: Test Basic Endpoints
```bash
# Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Get user profile (replace TOKEN with JWT from login)
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer TOKEN"
```

## 6. Key Changes Made

### Entities
- ✅ Converted from JPA `@Entity` to MongoDB `@Document`
- ✅ Changed IDs from `Long` to `String` (MongoDB ObjectId)
- ✅ Relationships now use ID references instead of foreign keys
- ✅ Added `authorId`, `postId`, `userId` fields for relationship tracking

### Repositories
- ✅ Changed from `JpaRepository` to `MongoRepository`
- ✅ Updated query methods to work with MongoDB document queries
- ✅ Removed JPA-specific `@Query` annotations

### Services
- ✅ Removed `@Transactional` annotations (not applicable to MongoDB)
- ✅ Updated ID parameters from `Long` to `String`
- ✅ Changed method signatures to use String IDs

### Controllers
- ✅ Updated path variables from `Long` to `String` for post IDs
- ✅ All API endpoints remain the same

### Dependencies
- ✅ Replaced `spring-boot-starter-data-jpa` with `spring-boot-starter-data-mongodb`
- ✅ Removed PostgreSQL driver
- ✅ Updated test dependencies

## 7. Data Migration (If you have existing data)

If you have existing PostgreSQL data to migrate:

1. **Export PostgreSQL Data**: Export your existing data as JSON
2. **Create MongoDB Collections**: The application will auto-create collections on first run
3. **Import Data**: Use MongoDB Atlas UI or mongoimport tool

```bash
mongoimport --uri "YOUR_MONGODB_ATLAS_URI" \
  --collection users \
  --file users.json \
  --jsonArray
```

## 8. Local Development with MongoDB

### Option 1: Use MongoDB Local (Recommended for Development)
```bash
# Install MongoDB locally
brew install mongodb-community

# Start MongoDB service
brew services start mongodb-community

# Update connection string to local
spring.data.mongodb.uri=mongodb://localhost:27017/axiom
```

### Option 2: Use MongoDB Atlas (Production-like)
Keep the cloud connection string as configured above

## 9. Troubleshooting

### Connection Issues
- Ensure your IP is whitelisted in MongoDB Atlas Network Access
- Verify the connection string includes the correct password
- Check that the database name is "axiom" (automatically created)

### Build Errors
- Run `mvn clean install` to clear any cached dependencies
- Ensure Java 21 is installed: `java -version`
- Check for any import errors related to JPA

### Runtime Errors
- Check application logs for connection errors
- Verify MongoDB Atlas cluster is running
- Ensure database user credentials are correct

## 10. Additional Resources

- [Spring Data MongoDB Documentation](https://spring.io/projects/spring-data-mongodb)
- [MongoDB Atlas Documentation](https://docs.atlas.mongodb.com/)
- [MongoDB Query Language](https://docs.mongodb.com/manual/reference/operator/query/)

## Summary

Your application is now fully configured to use MongoDB! The migration includes:
- ✅ All JPA/Hibernate dependencies replaced with MongoDB
- ✅ All entities converted to MongoDB documents
- ✅ All repositories using MongoRepository
- ✅ All services updated for MongoDB operations
- ✅ Configuration ready for MongoDB Atlas

Next steps:
1. Create your MongoDB Atlas cluster
2. Add environment variables or update application.properties
3. Build and run: `mvn clean install && mvn spring-boot:run`
4. Test the API endpoints
5. Enjoy your MongoDB-powered Axiom application!
