# 🚀 Free Hosting Guide for Smart Attend

This guide provides step-by-step instructions on how to host both the **Spring Boot Backend** and distribute the **Android Mobile App** completely **100% free of charge**.

---

## 📊 Overview of Free Hosting Providers

| Service | Component | Free Tier Features | Best For |
|---|---|---|---|
| **Render.com** | Spring Boot API | 512MB RAM, automatic GitHub deployment, SSL included | Quickest & easiest setup |
| **Oracle Cloud** | Backend + MySQL | 4 ARM OCPU cores, 24GB RAM, 200GB storage, **24/7 uptime (never sleeps)** | Production-grade free server |
| **Aiven.io / Clever Cloud** | MySQL Database | Free managed MySQL database instance | Remote MySQL hosting |
| **GitHub Releases** | Android APK | Unlimited downloads, public release hosting | Distributing APK to users |
| **Firebase App Distribution** | Android APK | Free internal testing & automated app delivery | Beta testing with students |

---

## 🛠️ Option 1: Hosting Backend on Render.com (Recommended - Easiest)

Render offers a free Web Service tier that connects directly to your GitHub repository and automatically builds your application using the provided `Dockerfile`.

### Step 1: Push Code to GitHub
1. Create a repository on GitHub (Public or Private).
2. Commit and push your `Smart_Attend` project to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit with Dockerfile"
   git remote add origin https://github.com/YOUR_USERNAME/Smart_Attend.git
   git push -u origin main
   ```

### Step 2: Create a Web Service on Render
1. Sign up / Log in to [Render.com](https://render.com/).
2. Click **New +** -> **Web Service**.
3. Connect your GitHub repository.
4. Set the root directory to `backend`.
5. Select **Docker** as the Runtime environment.
6. Choose the **Free** instance type.

### Step 3: Configure Environment Variables
In the Render dashboard, go to **Environment** and add:
* `SPRING_PROFILES_ACTIVE` = `dev` *(Uses embedded H2 database - zero configuration required)*
* `JWT_SECRET` = a Base64-encoded random secret of at least 32 bytes *(required for login token signing; generate one with `openssl rand -base64 32`)*
* OR for MySQL:
  * `SPRING_PROFILES_ACTIVE` = `mysql`
  * `SPRING_DATASOURCE_URL` = `jdbc:mysql://YOUR_MYSQL_HOST:3306/smart_attend?useSSL=true`
  * `SPRING_DATASOURCE_USERNAME` = `YOUR_DB_USER`
  * `SPRING_DATASOURCE_PASSWORD` = `YOUR_DB_PASSWORD`

The backend reads Render's `PORT` value (default `10000`) for its HTTP listener. With the `dev` profile, H2 is in-memory, so its data resets when the service restarts; use a managed database for persistent data.

### Step 4: Deploy & Get Backend URL
1. Click **Create Web Service**.
2. Render will build the Docker container and deploy your Spring Boot app.
3. Once deployed, Render provides a public HTTPS URL (e.g., `https://smart-attend-backend.onrender.com`).

---

## ⚡ Option 2: Hosting Backend on Oracle Cloud (Best Uptime & 24/7 Free)

Oracle Cloud Infrastructure (OCI) offers an **Always Free** tier with generous hardware specs that never sleep or spin down.

### Step 1: Create an OCI Always Free Account
1. Sign up at [Oracle Cloud Free Tier](https://www.oracle.com/cloud/free/).
2. Navigate to **Compute** -> **Instances** -> **Create Instance**.
3. Select **Ampere (ARM)** image with **Ubuntu 22.04 LTS**.
4. Allocate up to 4 OCPUs and 24 GB RAM (or 2 OCPUs / 12 GB RAM).
5. Download your SSH private key.

### Step 2: Setup Docker & MySQL on Instance
1. SSH into your VM:
   ```bash
   ssh -i your-key.key ubuntu@YOUR_SERVER_PUBLIC_IP
   ```
2. Install Docker and Docker Compose:
   ```bash
   sudo apt update && sudo apt install -y docker.io docker-compose
   sudo systemctl enable --now docker
   ```
3. Clone your repository and start the containers using Docker Compose or `docker run`.

---

## 🗄️ Free Database Options

If you prefer using a standalone MySQL database instead of the embedded H2 mode:

1. **Aiven.io (Free Tier)**:
   - Provides free managed MySQL or PostgreSQL database instances.
   - Copy connection URI into `SPRING_DATASOURCE_URL`.
2. **Clever Cloud**:
   - Offers free MySQL add-on (up to 5MB storage).
3. **Embedded H2 Database (Recommended for simple testing)**:
   - Set `SPRING_PROFILES_ACTIVE=dev` in your Spring Boot application.
   - Requires zero database setup!

---

## 📱 Option 3: Building & Distributing the Android App

Once your backend is hosted online, update the Android app configuration to connect to your live backend.

### Step 1: Update API Base URL
In `android/app/build.gradle`:
```groovy
def smartAttendBaseUrl = project.findProperty('smartAttendBaseUrl') ?: 'https://smart-attend-backend.onrender.com/'
```
*(Make sure to include the trailing slash `/`)*

Alternatively, users can change the server URL dynamically inside the app settings screen via `SessionManager`.

### Step 2: Build Release APK
Open a terminal in the `android` folder and run:
```bash
./gradlew assembleRelease
```
The output APK file will be generated at:
`android/app/build/outputs/apk/release/app-release-unsigned.apk`

*(Optional: Sign your APK using `apksigner` or configure `signingConfigs` in `build.gradle` for production use).*

### Step 3: Distribute APK for Free

#### Method A: GitHub Releases (Recommended for Public Sharing)
1. Go to your repository on GitHub.
2. Click **Releases** -> **Draft a new release**.
3. Attach your `app-release.apk` file.
4. Publish release. Users can download and install the APK directly on their Android devices.

#### Method B: Firebase App Distribution (Best for Classroom Testing)
1. Set up a free project on [Firebase Console](https://console.firebase.google.com/).
2. Enable **App Distribution**.
3. Upload your APK and add student/lecturer email addresses to send instant download invites.

---

## ✅ Verification Checklist

- [ ] Backend health check responds at `https://YOUR_BACKEND_URL/actuator/health` or `https://YOUR_BACKEND_URL/`
- [ ] Admin / Lecturer web dashboard is accessible at `https://YOUR_BACKEND_URL/admin.html`
- [ ] Android app connects to backend via HTTPS.
- [ ] QR code scanning & face verification function smoothly.
