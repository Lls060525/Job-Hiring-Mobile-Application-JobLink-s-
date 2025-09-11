package com.example.madassignment.data

// Common skills database categorized by field
val commonSkills = listOf(
    // Programming Languages
    "Java", "Python", "JavaScript", "C++", "C#", "Kotlin", "Swift", "TypeScript",
    "Ruby", "PHP", "Go", "Rust", "Scala", "Dart", "R", "MATLAB",

    // Web Development
    "React", "Angular", "Vue.js", "Node.js", "Express.js", "Next.js", "Nuxt.js",
    "Django", "Flask", "Spring Boot", "Ruby on Rails", "Laravel", "ASP.NET",
    "HTML", "CSS", "SASS", "LESS", "Bootstrap", "Tailwind CSS",

    // Mobile Development
    "React Native", "Flutter", "Android Development", "iOS Development", "Xamarin",

    // Databases
    "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite",
    "Cassandra", "Elasticsearch", "Firebase", "DynamoDB",

    // Cloud & DevOps
    "AWS", "Azure", "Google Cloud", "Docker", "Kubernetes", "Terraform",
    "Ansible", "Jenkins", "GitLab CI", "GitHub Actions", "Linux", "Shell Scripting",

    // Version Control
    "Git", "GitHub", "GitLab", "Bitbucket", "SVN",

    // Design & UI/UX
    "UI/UX Design", "Figma", "Adobe XD", "Sketch", "Photoshop", "Illustrator",
    "InDesign", "After Effects", "Premiere Pro", "Blender", "3D Modeling",

    // Project Management
    "Project Management", "Agile Methodology", "Scrum", "Kanban", "JIRA",
    "Trello", "Asana", "Confluence", "Waterfall", "PMBOK",

    // Data Science & AI
    "Machine Learning", "Data Science", "Artificial Intelligence", "Deep Learning",
    "TensorFlow", "PyTorch", "Keras", "Pandas", "NumPy", "SciPy", "Tableau",
    "Power BI", "Data Analysis", "Natural Language Processing", "Computer Vision",

    // Cybersecurity
    "Network Security", "Cybersecurity", "Ethical Hacking", "Penetration Testing",
    "Cryptography", "Firewall", "VPN", "Security Auditing", "OWASP",

    // Networking
    "TCP/IP", "DNS", "HTTP/HTTPS", "VPN", "Load Balancing", "CDN", "WebSocket",

    // APIs & Architecture
    "REST APIs", "GraphQL", "Microservices", "SOAP", "gRPC", "API Design",
    "System Design", "Design Patterns", "Clean Architecture",

    // Accounting & Finance
    "Accounting", "Bookkeeping", "Financial Analysis", "QuickBooks", "Xero",
    "SAP FICO", "Oracle Financials", "Tax Preparation", "Auditing", "Payroll",
    "Financial Modeling", "Excel Advanced", "Budgeting", "Forecasting",

    // Business & Management
    "Business Analysis", "Strategic Planning", "Risk Management", "Supply Chain",
    "Logistics", "Operations Management", "Six Sigma", "Lean Manufacturing",

    // Marketing & Sales
    "Digital Marketing", "SEO", "SEM", "Google Analytics", "Social Media Marketing",
    "Content Marketing", "Email Marketing", "CRM", "Salesforce", "HubSpot",
    "Market Research", "Brand Management", "Copywriting",

    // Content Creation
    "Content Writing", "Technical Writing", "Blogging", "Copyediting",
    "Video Editing", "Photography", "Graphic Design", "Podcasting",

    // Soft Skills
    "Leadership", "Team Management", "Problem Solving", "Communication Skills",
    "Time Management", "Critical Thinking", "Negotiation", "Public Speaking",
    "Presentation Skills", "Emotional Intelligence", "Conflict Resolution",

    // Education & Training
    "Teaching", "Curriculum Development", "E-learning", "Instructional Design",
    "Training Delivery", "Mentoring", "Coaching",

    // Healthcare & Medical
    "Patient Care", "Medical Coding", "HIPAA Compliance", "EMR Systems",
    "Clinical Research", "Pharmaceutical Knowledge", "Medical Terminology",

    // Legal
    "Legal Research", "Contract Law", "Intellectual Property", "Compliance",
    "Corporate Law", "Litigation", "Legal Writing",

    // Engineering (Non-Software)
    "Civil Engineering", "Mechanical Engineering", "Electrical Engineering",
    "Chemical Engineering", "CAD Design", "AutoCAD", "SolidWorks",

    // Languages
    "English", "Spanish", "French", "German", "Chinese", "Japanese", "Arabic",
    "Translation", "Interpretation",

    // Customer Service
    "Customer Support", "Help Desk", "Technical Support", "Customer Success",
    "Relationship Management"
)

val skillLevels = listOf("Beginner", "Intermediate", "Advanced", "Expert")

data class UserSkill(
    val name: String,
    val level: String
)

// Helper functions to convert between string and UserSkill list
fun skillsToString(skills: List<UserSkill>): String {
    return skills.joinToString(",") { "${it.name}:${it.level}" }
}

fun stringToSkills(skillsString: String): List<UserSkill> {
    if (skillsString.isBlank()) return emptyList()

    return skillsString.split(",").mapNotNull { skillStr ->
        val parts = skillStr.split(":")
        if (parts.size == 2) {
            UserSkill(parts[0], parts[1])
        } else if (parts.size == 1 && parts[0].isNotBlank()) {
            UserSkill(parts[0], "Intermediate")
        } else {
            null
        }
    }
}

