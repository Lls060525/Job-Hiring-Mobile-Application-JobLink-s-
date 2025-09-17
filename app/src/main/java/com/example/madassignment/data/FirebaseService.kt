package com.example.madassignment.data

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseService(private val context: Context) {
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val profilesCollection = db.collection("user_profiles")
    private val jobsCollection = db.collection("jobs")
    private val postsCollection = db.collection("community_posts")

    // User operations
    suspend fun addUser(user: User): String {
        val userData = hashMapOf(
            "id" to user.id,
            "email" to user.email,
            "password" to user.password,
            "name" to user.name,
            "isAdmin" to user.isAdmin,

        )
        val result = usersCollection.add(userData).await()

        // Note: Update your AppRepository class to include this method if needed
        // repository.updateUserFirestoreId(user.id, result.id)

        return result.id
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val querySnapshot = usersCollection.get().await()
            querySnapshot.documents.map { document ->
                User(
                    id = document.getLong("id")?.toInt() ?: 0,
                    email = document.getString("email") ?: "",
                    password = document.getString("password") ?: "",
                    name = document.getString("name") ?: "",
                    isAdmin = document.getBoolean("isAdmin") ?: false
                    // Remove createdAt and firestoreId if they don't exist in your User class
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            val userRef = usersCollection.document(userId)
            userRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun promoteUserToAdmin(userId: String): Boolean {
        return try {
            val userRef = usersCollection.document(userId)
            userRef.update("isAdmin", true).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteUserFromFirestore(userId: Int): Boolean {
        return try {
            // Delete user profile first
            val profileQuery = profilesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            profileQuery.documents.forEach { document ->
                profilesCollection.document(document.id).delete().await()
            }

            // Delete user
            val userQuery = usersCollection
                .whereEqualTo("id", userId)
                .get()
                .await()

            userQuery.documents.forEach { document ->
                usersCollection.document(document.id).delete().await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun promoteUserToAdmin(userId: Int): Boolean {
        return try {
            val userQuery = usersCollection
                .whereEqualTo("id", userId)
                .get()
                .await()

            if (!userQuery.isEmpty) {
                val document = userQuery.documents[0]
                usersCollection.document(document.id).update("isAdmin", true).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllUsersWithProfiles(): List<Pair<User, UserProfile?>> {
        return try {
            val users = getAllUsers()
            val result = mutableListOf<Pair<User, UserProfile?>>()

            for (user in users) {
                val profile = getUserProfileByUserId(user.id)
                result.add(Pair(user, profile))
            }

            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    // UserProfile operations
    suspend fun addUserProfile(profile: UserProfile): String {
        val profileData = hashMapOf(
            "userId" to profile.userId,
            "name" to profile.name,
            "age" to profile.age,
            "aboutMe" to profile.aboutMe,
            "skills" to profile.skills,
            "company" to profile.company,
            "profileImageUri" to profile.profileImageUri,
            "lastUpdated" to profile.lastUpdated,
            "isSetupComplete" to profile.isSetupComplete
        )
        val result = profilesCollection.add(profileData).await()
        return result.id
    }

    suspend fun getUserProfileByUserId(userId: Int): UserProfile? {
        return try {
            val querySnapshot = profilesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                UserProfile(
                    userId = document.getLong("userId")?.toInt() ?: 0,
                    name = document.getString("name") ?: "",
                    age = document.getString("age") ?: "",
                    aboutMe = document.getString("aboutMe") ?: "",
                    skills = document.getString("skills") ?: "",
                    company = document.getString("company") ?: "",
                    profileImageUri = document.getString("profileImageUri"),
                    lastUpdated = document.getDate("lastUpdated") ?: Date(),
                    isSetupComplete = document.getBoolean("isSetupComplete") ?: false
                    // Remove firestoreId if it doesn't exist in your UserProfile class
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Job operations
    suspend fun addJob(job: Job): String {
        return try {
            val jobData = hashMapOf(
                "title" to job.title,
                "company" to job.company,
                "location" to job.location,
                "type" to job.type,
                "salary" to job.salary,
                "category" to job.category,
                "requiredSkills" to job.requiredSkills,
                "originalJobId" to job.originalJobId,
                "createdAt" to Date() // Add timestamp
            )
            val result = jobsCollection.add(jobData).await()
            Log.d("FirebaseService", "Job added to Firestore: ${result.id}")
            result.id
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error adding job to Firestore: ${e.message}")
            e.printStackTrace()
            "" // Return empty string on error
        }
    }

    suspend fun addJobToFirestore(job: Job): Boolean {
        return try {
            val jobData = hashMapOf(
                "title" to job.title,
                "subtitle" to job.subtitle,
                "type" to job.type,
                "location" to job.location,
                "salary" to job.salary,
                "category" to job.category,
                "requiredSkills" to job.requiredSkills,
                "company" to job.company
            )
            jobsCollection.add(jobData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserJobs(userId: Int): List<Job> {
        return try {
            val querySnapshot = jobsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            querySnapshot.documents.map { document ->
                Job(
                    id = document.getLong("id")?.toInt() ?: 0,
                    title = document.getString("title") ?: "",
                    subtitle = document.getString("subtitle") ?: "",
                    type = document.getString("type") ?: "",
                    location = document.getString("location") ?: "",
                    salary = document.getString("salary") ?: "",
                    category = document.getString("category") ?: "",
                    isSaved = document.getBoolean("isSaved") ?: false,
                    isApplied = document.getBoolean("isApplied") ?: false,
                    userId = document.getLong("userId")?.toInt() ?: 0,
                    originalJobId = document.getLong("originalJobId")?.toInt() ?: 0,
                    requiredSkills = document.getString("requiredSkills") ?: "",
                    company = document.getString("company") ?: ""
                    // Remove firestoreId if it doesn't exist in your Job class
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun migrateAllJobsToFirestore(): Boolean {
        return try {
            val allJobs = getRecommendedJobs() + getNewJobs()

            for (job in allJobs) {
                // Check if job already exists in Firestore to avoid duplicates
                val existingJobs = jobsCollection
                    .whereEqualTo("originalJobId", job.originalJobId)
                    .get()
                    .await()

                if (existingJobs.isEmpty) {
                    val jobData = hashMapOf(
                        "title" to job.title,
                        "subtitle" to job.subtitle,
                        "type" to job.type,
                        "location" to job.location,
                        "salary" to job.salary,
                        "category" to job.category,
                        "isSaved" to false,
                        "isApplied" to false,
                        "userId" to 0, // System user
                        "originalJobId" to job.originalJobId,
                        "requiredSkills" to job.requiredSkills,
                        "company" to job.company,
                        "createdAt" to Date()
                    )
                    jobsCollection.add(jobData).await()
                    Log.d("FirebaseService", "Migrated job: ${job.title}")
                }
            }
            Log.d("FirebaseService", "All jobs migrated successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error migrating jobs: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // Post operations
    suspend fun addPost(post: CommunityPost): String {
        val postData = hashMapOf(
            "author" to post.author,
            "timeAgo" to "Just now", // Initial value
            "company" to post.company,
            "content" to post.content,
            "likes" to post.likes,
            "likedBy" to post.likedBy,
            "userId" to post.userId,
            "createdAt" to Date(), // Always use current date
            "lastUpdated" to Date()
        )

        val result = postsCollection.add(postData).await()
        return result.id
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            postsCollection.document(postId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getAllPosts(): List<CommunityPost> {
        return try {
            val querySnapshot = postsCollection
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.map { document ->
                CommunityPost(
                    id = document.id, // Store the ACTUAL Firebase document ID as string
                    author = document.getString("author") ?: "",
                    timeAgo = document.getString("timeAgo") ?: "",
                    company = document.getString("company") ?: "",
                    content = document.getString("content") ?: "",
                    likes = document.getLong("likes")?.toInt() ?: 0,
                    likedBy = document.getString("likedBy") ?: "",
                    userId = document.getLong("userId")?.toInt() ?: 0
                    // Remove createdAt if it doesn't exist in your CommunityPost class
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updatePostLikes(postId: String, likes: Int, likedBy: String) {
        val postRef = postsCollection.document(postId)
        postRef.update(
            mapOf(
                "likes" to likes,
                "likedBy" to likedBy
            )
        ).await()
    }

    suspend fun getPostById(postId: String): CommunityPost? {
        return try {
            val document = postsCollection.document(postId).get().await()
            if (document.exists()) {
                CommunityPost(
                    id = document.id, // Use actual document ID
                    author = document.getString("author") ?: "",
                    timeAgo = document.getString("timeAgo") ?: "",
                    company = document.getString("company") ?: "",
                    content = document.getString("content") ?: "",
                    likes = document.getLong("likes")?.toInt() ?: 0,
                    likedBy = document.getString("likedBy") ?: "",
                    userId = document.getLong("userId")?.toInt() ?: 0
                    // Remove createdAt if it doesn't exist in your CommunityPost class
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updatePostContent(postId: String, newContent: String): Boolean {
        return try {
            val postRef = postsCollection.document(postId)
            postRef.update(
                mapOf(
                    "content" to newContent,
                    "lastUpdated" to Date()
                )
            ).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun togglePostLike(postId: String, userId: Int, userName: String): Boolean {
        return try {
            val postRef = postsCollection.document(postId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likes")?.toInt() ?: 0
                var likedBy = snapshot.getString("likedBy") ?: ""

                val likedByList = if (likedBy.isBlank()) {
                    mutableListOf()
                } else {
                    likedBy.split(",").toMutableList()
                }

                // Check if user already liked (format: "userId:userName")
                val userLikeEntry = "$userId:$userName"
                val wasLiked = likedByList.any { it.startsWith("$userId:") }

                if (wasLiked) {
                    // Unlike: remove user entry and decrement count
                    likedByList.removeAll { it.startsWith("$userId:") }
                    transaction.update(postRef,
                        "likes", currentLikes - 1,
                        "likedBy", likedByList.joinToString(",")
                    )
                    false
                } else {
                    // Like: add user entry and increment count
                    likedByList.add(userLikeEntry)
                    transaction.update(postRef,
                        "likes", currentLikes + 1,
                        "likedBy", likedByList.joinToString(",")
                    )
                    true
                }
            }.await()

            true // Success
        } catch (e: Exception) {
            e.printStackTrace()
            false // Failure
        }
    }

    suspend fun togglePostLike(postId: String, userId: Int): Boolean {
        return try {
            val postRef = postsCollection.document(postId)

            // Use a transaction to ensure atomic operations
            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likes")?.toInt() ?: 0
                var likedBy = snapshot.getString("likedBy") ?: ""

                val likedByList = if (likedBy.isBlank()) {
                    mutableListOf()
                } else {
                    likedBy.split(",").toMutableList()
                }

                val userIdStr = userId.toString()
                val wasLiked = likedByList.contains(userIdStr)

                if (wasLiked) {
                    // Unlike: remove user ID and decrement count
                    likedByList.remove(userIdStr)
                    transaction.update(postRef,
                        "likes", currentLikes - 1,
                        "likedBy", likedByList.joinToString(",")
                    )
                    false
                } else {
                    // Like: add user ID and increment count
                    likedByList.add(userIdStr)
                    transaction.update(postRef,
                        "likes", currentLikes + 1,
                        "likedBy", likedByList.joinToString(",")
                    )
                    true
                }
            }.await()

            true // Success
        } catch (e: Exception) {
            e.printStackTrace()
            false // Failure
        }
    }

    suspend fun getPostLikes(postId: String): Pair<Int, List<String>> {
        return try {
            val document = postsCollection.document(postId).get().await()
            val likes = document.getLong("likes")?.toInt() ?: 0
            val likedBy = document.getString("likedBy") ?: ""

            // Parse format: "userId:userName,userId:userName"
            val likedByList = if (likedBy.isBlank()) {
                emptyList()
            } else {
                likedBy.split(",").map { entry ->
                    val parts = entry.split(":")
                    if (parts.size >= 2) parts[1] else "Unknown User"
                }
            }

            Pair(likes, likedByList)
        } catch (e: Exception) {
            Pair(0, emptyList())
        }
    }

    fun addPostsRealTimeListener(onPostsUpdated: (List<CommunityPost>) -> Unit) {
        postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                error?.let {
                    it.printStackTrace()
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val posts = querySnapshot.documents.map { document ->
                        CommunityPost(
                            id = document.id,
                            author = document.getString("author") ?: "",
                            timeAgo = "",
                            company = document.getString("company") ?: "",
                            content = document.getString("content") ?: "",
                            likes = document.getLong("likes")?.toInt() ?: 0,
                            likedBy = document.getString("likedBy") ?: "",
                            userId = document.getLong("userId")?.toInt() ?: 0
                            // Remove createdAt if it doesn't exist in your CommunityPost class
                        )
                    }
                    onPostsUpdated(posts)
                }
            }
    }

    // Helper functions for jobs
    private fun getRecommendedJobs(): List<Job> {
        return listOf(
            Job(
                id = 1001,
                title = "Senior Frontend Developer",
                subtitle = "React Specialist",
                type = "Full time",
                location = "Kuala Lumpur",
                salary = "RM 8,000 - RM 12,000",
                category = "Technology",
                originalJobId = 1001,
                requiredSkills = "JavaScript, React, HTML, CSS, TypeScript, Git, UI/UX Design, REST APIs",
                company = "Grab"
            ),
            Job(
                id = 1002,
                title = "Backend Engineer (Java/Spring)",
                subtitle = "Microservices Architecture",
                type = "Full time",
                location = "Cyberjaya",
                salary = "RM 9,000 - RM 13,000",
                category = "Technology",
                originalJobId = 1002,
                requiredSkills = "Java, Spring Boot, Microservices, SQL, Docker, Kubernetes, AWS, System Design",
                company = "Lazada"
            ),
            Job(
                id = 1003,
                title = "Full Stack Python Developer",
                subtitle = "Django & React",
                type = "Full time",
                location = "Petaling Jaya",
                salary = "RM 7,500 - RM 11,000",
                category = "Technology",
                originalJobId = 1003,
                requiredSkills = "Python, Django, React, JavaScript, PostgreSQL, REST APIs, Git",
                company = "Shopee"
            ),
            Job(
                id = 1004,
                title = "DevOps Engineer",
                subtitle = "Cloud Infrastructure",
                type = "Full time",
                location = "Bangsar South",
                salary = "RM 10,000 - RM 15,000",
                category = "Technology",
                originalJobId = 1004,
                requiredSkills = "AWS, Azure, Docker, Kubernetes, Terraform, Jenkins, Linux, Shell Scripting",
                company = "Google Cloud Malaysia"
            ),
            Job(
                id = 1005,
                title = "iOS Mobile Developer",
                subtitle = "SwiftUI Expert",
                type = "Full time",
                location = "Mont Kiara",
                salary = "RM 7,000 - RM 10,000",
                category = "Technology",
                originalJobId = 1005,
                requiredSkills = "Swift, iOS Development, SwiftUI, Xcode, REST APIs, Git",
                company = "Touch 'n Go Digital"
            ),
            Job(
                id = 1006,
                title = "Data Scientist",
                subtitle = "Machine Learning Focus",
                type = "Full time",
                location = "KL Sentral",
                salary = "RM 9,000 - RM 14,000",
                category = "Data & Analytics",
                originalJobId = 1006,
                requiredSkills = "Python, Machine Learning, TensorFlow, PyTorch, Pandas, SQL, Data Analysis",
                company = "Carsome"
            ),
            Job(
                id = 1007,
                title = "Business Intelligence Analyst",
                subtitle = "Data Visualization",
                type = "Full time",
                location = "Damansara Heights",
                salary = "RM 6,500 - RM 9,000",
                category = "Data & Analytics",
                originalJobId = 1007,
                requiredSkills = "SQL, Tableau, Power BI, Excel Advanced, Data Analysis, Business Analysis",
                company = "Maybank"
            ),
            Job(
                id = 1008,
                title = "AI Research Engineer",
                subtitle = "Natural Language Processing",
                type = "Full time",
                location = "Tun Razak Exchange",
                salary = "RM 11,000 - RM 16,000",
                category = "Data & Analytics",
                originalJobId = 1008,
                requiredSkills = "Python, Deep Learning, Natural Language Processing, TensorFlow, Research, AI",
                company = "Boost (Axiata Digital)"
            ),
            Job(
                id = 1009,
                title = "UI/UX Designer",
                subtitle = "Product Design",
                type = "Full time",
                location = "Bangsar",
                salary = "RM 5,500 - RM 8,000",
                category = "Design",
                originalJobId = 1009,
                requiredSkills = "UI/UX Design, Figma, Adobe XD, Prototyping, User Research, Wireframing",
                company = "Petal Ads (ByteDance)"
            ),
            Job(
                id = 1010,
                title = "Digital Marketing Manager",
                subtitle = "E-commerce Growth",
                type = "Full time",
                location = "Old Klang Road",
                salary = "RM 8,000 - RM 12,000",
                category = "Marketing",
                originalJobId = 1010,
                requiredSkills = "Digital Marketing, SEO, SEM, Google Analytics, Social Media Marketing, Strategy",
                company = "Lazada"
            ),
            Job(
                id = 1011,
                title = "Cloud Solutions Architect",
                subtitle = "AWS Specialist",
                type = "Full time",
                location = "Cyberjaya",
                salary = "RM 12,000 - RM 18,000",
                category = "Technology",
                originalJobId = 1011,
                requiredSkills = "AWS, Cloud Architecture, Solutions Design, DevOps, Infrastructure",
                company = "Amazon Web Services"
            ),
            Job(
                id = 1012,
                title = "Machine Learning Engineer",
                subtitle = "Computer Vision",
                type = "Full time",
                location = "KL Sentral",
                salary = "RM 10,000 - RM 15,000",
                category = "Data & Analytics",
                originalJobId = 1012,
                requiredSkills = "Python, Machine Learning, TensorFlow, PyTorch, Computer Vision, Deep Learning",
                company = "Sensetime Malaysia"
            ),
            Job(
                id = 1013,
                title = "Product Manager",
                subtitle = "Fintech Products",
                type = "Full time",
                location = "Bangsar South",
                salary = "RM 11,000 - RM 16,000",
                category = "Management",
                originalJobId = 1013,
                requiredSkills = "Product Management, Agile, Scrum, Market Research, Product Strategy",
                company = "BigPay"
            ),
            Job(
                id = 1014,
                title = "Network Engineer",
                subtitle = "Cisco Certified",
                type = "Full time",
                location = "Petaling Jaya",
                salary = "RM 7,000 - RM 10,000",
                category = "Technology",
                originalJobId = 1014,
                requiredSkills = "Networking, Cisco, TCP/IP, VPN, Network Security, CCNA",
                company = "Maxis"
            ),
            Job(
                id = 1015,
                title = "Database Administrator",
                subtitle = "Oracle DBA",
                type = "Full time",
                location = "Kuala Lumpur",
                salary = "RM 8,000 - RM 12,000",
                category = "Technology",
                originalJobId = 1015,
                requiredSkills = "Oracle, SQL, Database Administration, Performance Tuning, Backup & Recovery",
                company = "Oracle Malaysia"
            ),
            Job(
                id = 1016,
                title = "Game Developer",
                subtitle = "Unity Expert",
                type = "Full time",
                location = "Mont Kiara",
                salary = "RM 6,000 - RM 9,000",
                category = "Technology",
                originalJobId = 1016,
                requiredSkills = "Unity, C#, Game Development, 3D Graphics, Mobile Games",
                company = "Sea Group (Garena)"
            ),
            Job(
                id = 1017,
                title = "Blockchain Developer",
                subtitle = "Smart Contracts",
                type = "Full time",
                location = "Tun Razak Exchange",
                salary = "RM 9,000 - RM 14,000",
                category = "Technology",
                originalJobId = 1017,
                requiredSkills = "Blockchain, Solidity, Ethereum, Smart Contracts, Web3, Cryptography",
                company = "Tokenize Malaysia"
            ),
            Job(
                id = 1018,
                title = "QA Automation Engineer",
                subtitle = "Selenium Expert",
                type = "Full time",
                location = "Petaling Jaya",
                salary = "RM 6,500 - RM 9,500",
                category = "Technology",
                originalJobId = 1018,
                requiredSkills = "Selenium, Java, Test Automation, QA, JUnit, TestNG",
                company = "IBM Malaysia"
            ),
            Job(
                id = 1019,
                title = "Technical Lead",
                subtitle = "Java Spring Boot",
                type = "Full time",
                location = "Cyberjaya",
                salary = "RM 13,000 - RM 18,000",
                category = "Technology",
                originalJobId = 1019,
                requiredSkills = "Java, Spring Boot, Leadership, Architecture, Team Management, Microservices",
                company = "HSBC Technology"
            ),
            Job(
                id = 1020,
                title = "Embedded Systems Engineer",
                subtitle = "IoT Devices",
                type = "Full time",
                location = "Penang",
                salary = "RM 7,000 - RM 10,000",
                category = "Technology",
                originalJobId = 1020,
                requiredSkills = "C++, Embedded Systems, IoT, Microcontrollers, RTOS, Electronics",
                company = "Intel Penang"
            )
        )
    }

    private fun getNewJobs(): List<Job> {
        return listOf(
            Job(
                id = 2001,
                title = "Content Marketing Specialist",
                subtitle = "SEO Writing",
                type = "Full time",
                location = "Kota Damansara",
                salary = "RM 5,000 - RM 7,000",
                category = "Marketing",
                originalJobId = 2001,
                requiredSkills = "Content Writing, SEO, Copywriting, Blogging, Content Marketing",
                company = "iPrice Group"
            ),
            Job(
                id = 2002,
                title = "Financial Analyst",
                subtitle = "Corporate Finance",
                type = "Full time",
                location = "Tun Razak Exchange",
                salary = "RM 7,000 - RM 10,000",
                category = "Finance",
                originalJobId = 2002,
                requiredSkills = "Financial Analysis, Excel Advanced, Financial Modeling, Budgeting, Forecasting",
                company = "Public Bank Berhad"
            ),
            Job(
                id = 2003,
                title = "Project Manager (IT)",
                subtitle = "Agile Scrum Master",
                type = "Full time",
                location = "Mid Valley",
                salary = "RM 10,000 - RM 15,000",
                category = "Management",
                originalJobId = 2003,
                requiredSkills = "Project Management, Agile Methodology, Scrum, JIRA, Team Management",
                company = "Maxis"
            ),
            Job(
                id = 2004,
                title = "Cybersecurity Analyst",
                subtitle = "Threat Detection",
                type = "Full time",
                location = "Cyberjaya",
                salary = "RM 8,000 - RM 12,000",
                category = "Security",
                originalJobId = 2004,
                requiredSkills = "Cybersecurity, Network Security, Firewall, VPN, Security Auditing",
                company = "Bank Negara Malaysia"
            ),
            Job(
                id = 2005,
                title = "HR Business Partner",
                subtitle = "Talent Development",
                type = "Full time",
                location = "Damansara Perdana",
                salary = "RM 7,000 - RM 10,000",
                category = "Human Resources",
                originalJobId = 2005,
                requiredSkills = "Talent Management, Employee Relations, Strategic Planning, Communication Skills",
                company = "Petronas"
            ),
            Job(
                id = 2006,
                title = "Graphic Designer",
                subtitle = "Brand & Marketing",
                type = "Full time",
                location = "Subang Jaya",
                salary = "RM 4,000 - RM 6,000",
                category = "Design",
                originalJobId = 2006,
                requiredSkills = "Photoshop, Illustrator, InDesign, Graphic Design, Brand Management",
                company = "Media Prima Digital"
            ),
            Job(
                id = 2007,
                title = "Salesforce Administrator",
                subtitle = "CRM Management",
                type = "Full time",
                location = "KL City Centre",
                salary = "RM 6,000 - RM 9,000",
                category = "Marketing",
                originalJobId = 2007,
                requiredSkills = "Salesforce, CRM, Business Analysis, Data Analysis, Process Improvement",
                company = "CIMB Bank"
            ),
            Job(
                id = 2008,
                title = "Accountant",
                subtitle = "Audit & Compliance",
                type = "Full time",
                location = "Pudu",
                salary = "RM 5,000 - RM 7,500",
                category = "Finance",
                originalJobId = 2008,
                requiredSkills = "Accounting, QuickBooks, Tax Preparation, Auditing, Financial Reporting",
                company = "Deloitte Malaysia"
            ),
            Job(
                id = 2009,
                title = "Business Development Manager",
                subtitle = "Strategic Partnerships",
                type = "Full time",
                location = "Bangsar South",
                salary = "RM 9,000 - RM 13,000 + Commission",
                category = "Business",
                originalJobId = 2009,
                requiredSkills = "Business Analysis, Strategic Planning, Negotiation, Relationship Management",
                company = "Foodpanda Malaysia"
            ),
            Job(
                id = 2010,
                title = "Technical Support Engineer",
                subtitle = "Enterprise Software",
                type = "Full time",
                location = "Sunway",
                salary = "RM 4,500 - RM 6,500",
                category = "Support",
                originalJobId = 2010,
                requiredSkills = "Technical Support, Customer Support, Linux, SQL, Problem Solving",
                company = "Jabil Circuit"
            ),
            Job(
                id = 2011,
                title = "Social Media Manager",
                subtitle = "Content Strategy",
                type = "Full time",
                location = "Bangsar",
                salary = "RM 5,500 - RM 8,000",
                category = "Marketing",
                originalJobId = 2011,
                requiredSkills = "Social Media, Content Strategy, Analytics, Community Management, Digital Marketing",
                company = "Mindvalley"
            ),
            Job(
                id = 2012,
                title = "Supply Chain Analyst",
                subtitle = "Logistics Optimization",
                type = "Full time",
                location = "Port Klang",
                salary = "RM 6,000 - RM 8,500",
                category = "Logistics",
                originalJobId = 2012,
                requiredSkills = "Supply Chain, Logistics, Data Analysis, Inventory Management, SAP",
                company = "Westports Malaysia"
            ),
            Job(
                id = 2013,
                title = "Legal Counsel",
                subtitle = "Corporate Law",
                type = "Full time",
                location = "KL City Centre",
                salary = "RM 12,000 - RM 18,000",
                category = "Legal",
                originalJobId = 2013,
                requiredSkills = "Corporate Law, Contract Law, Legal Research, Compliance, Negotiation",
                company = "Shell Malaysia"
            ),
            Job(
                id = 2014,
                title = "Clinical Research Associate",
                subtitle = "Pharmaceutical Trials",
                type = "Full time",
                location = "Petaling Jaya",
                salary = "RM 7,000 - RM 10,000",
                category = "Healthcare",
                originalJobId = 2014,
                requiredSkills = "Clinical Research, GCP, Protocol Development, Data Management, Healthcare",
                company = "Pfizer Malaysia"
            ),
            Job(
                id = 2015,
                title = "Interior Designer",
                subtitle = "Residential Projects",
                type = "Full time",
                location = "Damansara",
                salary = "RM 4,500 - RM 7,000",
                category = "Design",
                originalJobId = 2015,
                requiredSkills = "Interior Design, AutoCAD, 3D Modeling, Space Planning, Project Management",
                company = "IDC Design"
            ),
            Job(
                id = 2016,
                title = "Event Coordinator",
                subtitle = "Corporate Events",
                type = "Full time",
                location = "KL City Centre",
                salary = "RM 4,000 - RM 6,000",
                category = "Events",
                originalJobId = 2016,
                requiredSkills = "Event Planning, Coordination, Vendor Management, Logistics, Communication",
                company = "KL Convention Centre"
            ),
            Job(
                id = 2017,
                title = "English Teacher",
                subtitle = "ESL Specialist",
                type = "Full time",
                location = "Kuala Lumpur",
                salary = "RM 5,000 - RM 7,500",
                category = "Education",
                originalJobId = 2017,
                requiredSkills = "Teaching, ESL, Curriculum Development, Communication, Lesson Planning",
                company = "British Council Malaysia"
            ),
            Job(
                id = 2018,
                title = "Real Estate Agent",
                subtitle = "Residential Properties",
                type = "Full time",
                location = "Mont Kiara",
                salary = "RM 3,000 + Commission",
                category = "Real Estate",
                originalJobId = 2018,
                requiredSkills = "Sales, Negotiation, Property Market, Customer Service, Communication",
                company = "IQI Realty"
            ),
            Job(
                id = 2019,
                title = "Chef de Partie",
                subtitle = "Western Cuisine",
                type = "Full time",
                location = "Bukit Bintang",
                salary = "RM 3,500 - RM 5,000",
                category = "Hospitality",
                originalJobId = 2019,
                requiredSkills = "Culinary Arts, Food Preparation, Kitchen Management, Western Cuisine, Hygiene Standards",
                company = "Marriott Hotel Kuala Lumpur"
            ),
            Job(
                id = 2020,
                title = "Fitness Trainer",
                subtitle = "Personal Training",
                type = "Full time",
                location = "Bangsar",
                salary = "RM 3,000 - RM 6,000 + Commission",
                category = "Fitness",
                originalJobId = 2020,
                requiredSkills = "Personal Training, Fitness Assessment, Nutrition, Exercise Science, Motivation",
                company = "Celebrity Fitness"
            )
        )
    }
}