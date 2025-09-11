package com.example.madassignment.data

import android.content.Context
import android.util.Log

class AppRepository(context: Context) {
    private val appDao = AppDatabase.getDatabase(context).appDao()

    // User operations
    suspend fun registerUser(email: String, password: String, name: String): Long {
        return try {
            val user = User(email = email, password = password, name = name)
            val userId = appDao.insertUser(user)
            Log.d("AppRepository", "Registered user: $email with ID: $userId")
            userId
        } catch (e: Exception) {
            Log.e("AppRepository", "Error registering user: ${e.message}")
            -1
        }
    }

    suspend fun loginUser(email: String, password: String): User? {
        return try {
            val user = appDao.getUser(email, password)
            Log.d("AppRepository", "Login attempt: $email - ${if (user != null) "Success" else "Failed"}")
            user
        } catch (e: Exception) {
            Log.e("AppRepository", "Error logging in: ${e.message}")
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            val user = appDao.getUserByEmail(email)
            Log.d("AppRepository", "Get user by email: $email - ${if (user != null) "Found" else "Not found"}")
            user
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting user by email: ${e.message}")
            null
        }
    }

    suspend fun getUserById(userId: Int): User? {
        return try {
            val user = appDao.getUserById(userId)
            Log.d("AppRepository", "Get user by ID: $userId - ${if (user != null) "Found" else "Not found"}")
            user
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting user by ID: ${e.message}")
            null
        }
    }

    // User profile operations
    suspend fun saveUserProfile(profile: UserProfile): Long {
        return try {
            val existingProfile = appDao.getUserProfile(profile.userId)
            val result = if (existingProfile != null) {
                appDao.updateUserProfile(profile)
                profile.userId.toLong()
            } else {
                appDao.insertUserProfile(profile)
            }
            Log.d("AppRepository", "Saved user profile for userId: ${profile.userId}, result: $result")
            result
        } catch (e: Exception) {
            Log.e("AppRepository", "Error saving user profile: ${e.message}")
            -1
        }
    }

    suspend fun getUserProfile(userId: Int): UserProfile? {
        return try {
            val profile = appDao.getUserProfile(userId)
            Log.d("AppRepository", "Retrieved profile for user $userId: $profile")
            profile
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting user profile: ${e.message}")
            null
        }
    }

    // Job operations
    suspend fun saveUserJob(job: Job, userId: Int): Long {
        return try {
            // Check if job already exists for this user
            val existingJob = appDao.getJobByOriginalId(userId, job.originalJobId)
            val result = if (existingJob != null) {
                // Update existing job
                val updatedJob = existingJob.copy(isSaved = true)
                appDao.updateJob(updatedJob)
                updatedJob.id.toLong()
            } else {
                // Insert new job
                val userJob = job.copy(userId = userId, isSaved = true)
                appDao.insertJob(userJob)
            }
            Log.d("AppRepository", "Saved job for userId: $userId, jobId: ${job.id}, result: $result")
            result
        } catch (e: Exception) {
            Log.e("AppRepository", "Error saving user job: ${e.message}")
            -1
        }
    }

    suspend fun applyToUserJob(job: Job, userId: Int): Long {
        return try {
            // Check if job already exists for this user
            val existingJob = appDao.getJobByOriginalId(userId, job.originalJobId)
            val result = if (existingJob != null) {
                // Update existing job
                val updatedJob = existingJob.copy(isApplied = true)
                appDao.updateJob(updatedJob)
                updatedJob.id.toLong()
            } else {
                // Insert new job
                val userJob = job.copy(userId = userId, isApplied = true)
                appDao.insertJob(userJob)
            }
            Log.d("AppRepository", "Applied to job for userId: $userId, jobId: ${job.id}, result: $result")
            result
        } catch (e: Exception) {
            Log.e("AppRepository", "Error applying to job: ${e.message}")
            -1
        }
    }

    suspend fun getUserSavedJobs(userId: Int): List<Job> {
        return try {
            val jobs = appDao.getUserSavedJobs(userId)
            Log.d("AppRepository", "Retrieved ${jobs.size} saved jobs for userId: $userId")
            jobs
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting saved jobs: ${e.message}")
            emptyList()
        }
    }


    suspend fun removeAppliedJob(userId: Int, jobId: Int) {
        try {
            appDao.removeAppliedJob(userId, jobId)  // This should UPDATE, not DELETE
            Log.d("AppRepository", "Removed applied flag from job: $jobId for userId: $userId")
        } catch (e: Exception) {
            Log.e("AppRepository", "Error removing applied job: ${e.message}")
        }
    }
    suspend fun getUserAppliedJobs(userId: Int): List<Job> {
        return try {
            val jobs = appDao.getUserAppliedJobs(userId)
            Log.d("AppRepository", "Retrieved ${jobs.size} applied jobs for userId: $userId")
            jobs
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting applied jobs: ${e.message}")
            emptyList()
        }
    }

    suspend fun removeSavedJob(userId: Int, jobId: Int) {
        try {
            appDao.removeSavedJob(userId, jobId)  // This should UPDATE, not DELETE
            Log.d("AppRepository", "Removed saved flag from job: $jobId for userId: $userId")
        } catch (e: Exception) {
            Log.e("AppRepository", "Error removing saved job: ${e.message}")
        }
    }

    // Community operations
    suspend fun getUserPosts(userId: Int): List<CommunityPost> {
        return try {
            val posts = appDao.getUserPosts(userId)
            Log.d("AppRepository", "Retrieved ${posts.size} posts for userId: $userId")
            posts
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting user posts: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllCommunityPosts(): List<CommunityPost> {
        return try {
            val posts = appDao.getAllCommunityPosts()
            Log.d("AppRepository", "Retrieved ${posts.size} community posts")
            posts
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting community posts: ${e.message}")
            emptyList()
        }
    }

    suspend fun addUserPost(post: CommunityPost, userId: Int): Long {
        return try {
            val userPost = post.copy(userId = userId)
            val postId = appDao.insertPost(userPost)
            Log.d("AppRepository", "Added post for userId: $userId, postId: $postId")
            postId
        } catch (e: Exception) {
            Log.e("AppRepository", "Error adding user post: ${e.message}")
            -1
        }
    }

    suspend fun updatePost(post: CommunityPost) {
        try {
            appDao.updatePost(post)
            Log.d("AppRepository", "Updated post: ${post.id}")
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating post: ${e.message}")
        }
    }

    suspend fun getPostById(postId: Int): CommunityPost? {
        return try {
            val post = appDao.getPostById(postId)
            Log.d("AppRepository", "Retrieved post by ID: $postId - ${if (post != null) "Found" else "Not found"}")
            post
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting post by ID: ${e.message}")
            null
        }
    }
    suspend fun initializeUserSampleData(userId: Int) {
        try {
            // Only check if profile exists, don't create it automatically
            val existingProfile = appDao.getUserProfile(userId)
            if (existingProfile == null) {
                Log.d("AppRepository", "No profile exists for user $userId - will show setup screen")
                // Don't create profile here - let the setup screen handle it
            } else {
                Log.d("AppRepository", "Profile already exists for user $userId: $existingProfile")
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error checking user profile: ${e.message}")
        }
    }
    // Skill-based job recommendations
    suspend fun getRecommendedJobsBasedOnSkills(skills: String): List<Job> {
        return try {
            val userSkills = skills.lowercase().split(",").map { it.trim() }
            val allJobs = getRecommendedJobs() + getNewJobs()

            val filteredJobs = allJobs.filter { job ->
                // Match jobs based on skills in title, category, and requiredSkills
                val jobText = (job.title + " " + job.category + " " + job.requiredSkills).lowercase()

                userSkills.any { skill ->
                    skill.isNotBlank() && jobText.contains(skill)
                }
            }

            val result = if (filteredJobs.isNotEmpty()) {
                filteredJobs
            } else {
                // Fallback to default recommendations if no matches
                getRecommendedJobs().take(3)
            }

            Log.d("AppRepository", "Skill-based recommendations: ${result.size} jobs found for skills: $skills")
            result
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting skill-based jobs: ${e.message}")
            getRecommendedJobs().take(3)
        }
    }
}

fun getRecommendedJobs(): List<Job> {
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

fun getNewJobs(): List<Job> {
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

fun getCommunityPosts(): List<CommunityPost> {
    return listOf(
        CommunityPost(
            author = "Alex Lee",
            timeAgo = "16d ago",
            company = "Masks Enterprise",
            content = "This is the most interesting company..."
        ),
        CommunityPost(
            author = "Yong Yee Hui",
            timeAgo = "1d ago",
            company = "Digi Enterprise",
            content = "It is understood that this is a good company..."
        )
    )
}