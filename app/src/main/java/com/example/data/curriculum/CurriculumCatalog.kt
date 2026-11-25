package com.example.data.curriculum

import com.example.model.Concept3DScene
import com.example.model.ExpertTutorProfile
import com.example.model.LearningTrack
import com.example.model.LessonStep
import com.example.model.MilestoneReward
import com.example.model.PracticeTestPack
import com.example.model.PrimitiveShape3D
import com.example.model.QuestLesson
import com.example.model.QuizQuestion
import com.example.model.SceneNode3D
import com.example.model.TrackCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CurriculumCatalog {

    val tracks: List<LearningTrack> = listOf(
        // School Classes 1 to 12
        LearningTrack("class_1", "Class 1", "Cls 1", TrackCategory.SCHOOL, "Foundations: Numbers, Phonics & Nature", listOf("Mathematics", "English", "EVS")),
        LearningTrack("class_2", "Class 2", "Cls 2", TrackCategory.SCHOOL, "Addition, Reading & Living World", listOf("Mathematics", "English", "EVS")),
        LearningTrack("class_3", "Class 3", "Cls 3", TrackCategory.SCHOOL, "Multiplication, Fractions & Our Earth", listOf("Mathematics", "Science", "English", "Social Studies")),
        LearningTrack("class_4", "Class 4", "Cls 4", TrackCategory.SCHOOL, "Factors, Matter & Early History", listOf("Mathematics", "Science", "English", "Social Studies")),
        LearningTrack("class_5", "Class 5", "Cls 5", TrackCategory.SCHOOL, "Fractions, Decimals, Cells & Solar System", listOf("Mathematics", "Science", "English", "Social Studies")),
        LearningTrack("class_6", "Class 6", "Cls 6", TrackCategory.SCHOOL, "Algebra Intro, Light, Magnets & Civics", listOf("Mathematics", "Science", "History", "Geography", "English")),
        LearningTrack("class_7", "Class 7", "Cls 7", TrackCategory.SCHOOL, "Integers, Acids-Bases, Nutrition & Empires", listOf("Mathematics", "Science", "History", "Geography", "English")),
        LearningTrack("class_8", "Class 8", "Cls 8", TrackCategory.SCHOOL, "Linear Equations, Cells, Force & Constitution", listOf("Mathematics", "Science", "History", "Civics", "English")),
        LearningTrack("class_9", "Class 9", "Cls 9", TrackCategory.SCHOOL, "Geometry, Atoms, Motion & World History", listOf("Mathematics", "Science", "Social Science", "English", "Computer Science")),
        LearningTrack("class_10", "Class 10", "Cls 10", TrackCategory.SCHOOL, "Trigonometry, Chemical Reactions, DNA & Boards", listOf("Mathematics", "Science", "Social Science", "English", "Computer Science")),
        LearningTrack("class_11", "Class 11", "Cls 11", TrackCategory.SCHOOL, "Calculus, Kinematics, Atomic Orbitals & Macro", listOf("Physics", "Chemistry", "Mathematics", "Biology", "Economics", "Computer Science")),
        LearningTrack("class_12", "Class 12", "Cls 12", TrackCategory.SCHOOL, "Electromagnetism, Genetics, Integrals & Boards", listOf("Physics", "Chemistry", "Mathematics", "Biology", "Economics", "Computer Science")),

        // Government Job Exams
        LearningTrack("upsc_cse", "UPSC CSE", "UPSC", TrackCategory.GOVT_EXAM, "Civil Services: Polity, Economy, GS & CSAT", listOf("Polity", "History", "Geography", "Economy", "General Science", "CSAT Aptitude")),
        LearningTrack("ssc_cgl", "SSC CGL / CHSL", "SSC", TrackCategory.GOVT_EXAM, "Quant Speed Math, Reasoning, English & GK", listOf("Quantitative Aptitude", "Reasoning", "General Awareness", "English")),
        LearningTrack("banking_ibps", "Banking IBPS / SBI", "Bank", TrackCategory.GOVT_EXAM, "Data Interpretation, Puzzles & Financial GK", listOf("Quantitative Aptitude", "Reasoning", "Banking Awareness", "English")),
        LearningTrack("railways_rrb", "Railways RRB NTPC", "RRB", TrackCategory.GOVT_EXAM, "General Science, Arithmetic & Current Affairs", listOf("General Science", "Mathematics", "Reasoning", "General Awareness")),
        LearningTrack("ctet_teaching", "Teaching CTET", "CTET", TrackCategory.GOVT_EXAM, "Child Pedagogy, Math, EVS & Language", listOf("Child Pedagogy", "Mathematics", "Environmental Studies", "English")),
        LearningTrack("defence_nda", "Defence NDA / CDS", "NDA", TrackCategory.GOVT_EXAM, "Higher Math, Physics, Geography & Defence GK", listOf("Mathematics", "Physics", "General Studies", "English"))
    )

    val milestones: List<MilestoneReward> = listOf(
        MilestoneReward("first_lesson", "First Quest Complete", "Finish your very first playable lesson.", 5, 10L),
        MilestoneReward("xp_100", "Century Scholar", "Earn 100 total XP points.", 8, 100L),
        MilestoneReward("xp_500", "Knowledge Explorer", "Reach 500 total XP points.", 15, 500L),
        MilestoneReward("visualizer_3d", "3D Visionary", "Explore a concept in interactive 3D.", 6, 0L),
        MilestoneReward("quiz_ace", "Checkpoint Ace", "Score 100% on a lesson or practice test.", 10, 0L),
        MilestoneReward("planner_pro", "Routine Builder", "Create or complete a study schedule task.", 6, 0L),
        MilestoneReward("curious_mind", "Researcher", "Ask Owl a Google-researched question.", 5, 0L)
    )

    val tutors: List<ExpertTutorProfile> = listOf(
        ExpertTutorProfile(
            id = "tutor_meera",
            name = "Dr. Meera Nair",
            specialty = "Physics & Calculus (Class 9–12 / NDA)",
            subjects = listOf("Physics", "Science", "Mathematics"),
            experienceYears = 11,
            rating = "4.9",
            availabilityLabel = "Online now",
            onlineNow = true,
            bio = "PhD in Applied Physics. Specializes in visual intuition for electromagnetism, mechanics, and calculus."
        ),
        ExpertTutorProfile(
            id = "tutor_arjun",
            name = "Arjun Verma",
            specialty = "UPSC Polity, Economy & GS Mentor",
            subjects = listOf("Polity", "Economy", "Social Science", "General Awareness", "History"),
            experienceYears = 8,
            rating = "4.9",
            availabilityLabel = "Online now",
            onlineNow = true,
            bio = "Cleared UPSC Mains 3 times; simplifies constitutional articles, fiscal policy, and current affairs."
        ),
        ExpertTutorProfile(
            id = "tutor_kavya",
            name = "Kavya Sharma",
            specialty = "Chemistry & Biology (Class 6–12)",
            subjects = listOf("Chemistry", "Biology", "Science", "EVS"),
            experienceYears = 7,
            rating = "4.8",
            availabilityLabel = "Replies in ~3 min",
            onlineNow = true,
            bio = "Biochemistry educator turning organic mechanisms, DNA genetics, and atomic orbitals into visual stories."
        ),
        ExpertTutorProfile(
            id = "tutor_rohan",
            name = "Rohan Kulkarni",
            specialty = "SSC / Banking Quant & Reasoning",
            subjects = listOf("Quantitative Aptitude", "Reasoning", "Mathematics", "CSAT Aptitude"),
            experienceYears = 9,
            rating = "4.9",
            availabilityLabel = "Slot open today",
            onlineNow = false,
            bio = "Speed-math coach teaching 20-second shortcut methods for percentages, DI, ratios, and syllogisms."
        ),
        ExpertTutorProfile(
            id = "tutor_ananya",
            name = "Ananya Bose",
            specialty = "English Grammar & Child Pedagogy",
            subjects = listOf("English", "Child Pedagogy"),
            experienceYears = 6,
            rating = "4.8",
            availabilityLabel = "Online now",
            onlineNow = true,
            bio = "TESOL-certified mentor for Class 1–12 comprehension, essay structure, and CTET pedagogy."
        )
    )

    val builtIn3DConcepts: List<Concept3DScene> = listOf(
        buildBohrAtomScene(),
        buildDnaHelixScene(),
        buildSolarSystemScene(),
        buildGeometrySolidsScene(),
        buildEarthLayersScene(),
        buildPlantCellScene(),
        buildElectromagneticWaveScene()
    )

    fun getBuiltInLessonsFor(trackId: String, subject: String): List<QuestLesson> {
        val exact = seedLessons.filter { it.trackId == trackId && it.subject.equals(subject, ignoreCase = true) }
        if (exact.isNotEmpty()) return exact

        val track = tracks.firstOrNull { it.id == trackId } ?: tracks.first()
        val chosenSubject = subject.ifBlank { track.subjects.first() }
        return buildUniversalLessonsForTrack(track, chosenSubject)
    }

    fun getBuiltInTestsFor(trackId: String, subject: String): List<PracticeTestPack> {
        val track = tracks.firstOrNull { it.id == trackId } ?: tracks.first()
        val chosenSubject = subject.ifBlank { track.subjects.first() }
        return listOf(
            PracticeTestPack(
                id = "test_${track.id}_${chosenSubject.lowercase().replace(" ", "_")}_1",
                trackId = track.id,
                subject = chosenSubject,
                title = "${track.shortTitle} $chosenSubject Sprint",
                difficulty = if (track.category == TrackCategory.GOVT_EXAM) "Exam Level" else "Core Mastery",
                durationMinutes = 8,
                questions = buildDefaultQuestions(track, chosenSubject),
                savedOffline = true
            ),
            PracticeTestPack(
                id = "test_mixed_3d_foundation",
                trackId = track.id,
                subject = "General Science & Logic",
                title = "Visual Science & Reasoning Mock",
                difficulty = "Standard",
                durationMinutes = 10,
                questions = mixedVisualQuestions,
                savedOffline = true
            )
        )
    }

    private fun buildUniversalLessonsForTrack(track: LearningTrack, subject: String): List<QuestLesson> {
        val levelLabel = track.title
        val isGovt = track.category == TrackCategory.GOVT_EXAM
        val linked3d = when {
            subject.contains("Science", true) || subject.contains("Chem", true) -> "atom_bohr"
            subject.contains("Bio", true) || subject.contains("EVS", true) -> "dna_helix"
            subject.contains("Geo", true) -> "earth_layers"
            subject.contains("Phy", true) -> "em_wave"
            subject.contains("Math", true) || subject.contains("Quant", true) || subject.contains("CSAT", true) -> "geometry_solids"
            else -> "solar_system"
        }

        val lesson1 = QuestLesson(
            id = "seed_${track.id}_${subject.lowercase().replace(" ", "_")}_1",
            trackId = track.id,
            subject = subject,
            title = if (isGovt) "$subject Core Foundations" else "$subject: Core Concepts",
            description = "Master high-yield $subject principles for $levelLabel with Owl's interactive checkpoints.",
            durationMinutes = 6,
            xpReward = 45,
            orderIndex = 0,
            concept3dId = linked3d,
            steps = listOf(
                LessonStep(
                    title = "Big Picture in $subject",
                    owlPrompt = "Welcome to **$levelLabel — $subject**! Every complex topic becomes easy when you anchor the core rule first, visualize it, and test it on a quick example.",
                    keyFormulaOrFact = when {
                        subject.contains("Math", true) || subject.contains("Quant", true) -> "Ratio & Fraction Anchor: Part / Whole = % / 100"
                        subject.contains("Polity", true) -> "Constitutional Pillar: Fundamental Rights (Articles 12–35)"
                        subject.contains("Chem", true) -> "Atomic Number (Z) = Protons = Neutral Electrons"
                        subject.contains("Phy", true) -> "Newton's 2nd Law: F = m × a"
                        subject.contains("Bio", true) -> "Base Pairing Rule: A pairs with T, C pairs with G"
                        else -> "Concept -> Visual Model -> Recall Quiz"
                    },
                    checkpointQuestion = buildDefaultQuestions(track, subject)[0]
                ),
                LessonStep(
                    title = "Active Application",
                    owlPrompt = "Nice! Now let's apply that idea to a real $levelLabel question so it sticks in long-term memory.",
                    keyFormulaOrFact = "Always check units, keywords ('except', 'only', 'not'), and eliminate two distractors first.",
                    checkpointQuestion = buildDefaultQuestions(track, subject)[1]
                ),
                LessonStep(
                    title = "Visual Synthesis & Mastery",
                    owlPrompt = "You can open the **3D Concept Lab** anytime from this lesson to rotate models and inspect parts in 3D space!",
                    keyFormulaOrFact = "Spaced repetition: revisit this quest after 24 hours for 90%+ retention.",
                    checkpointQuestion = buildDefaultQuestions(track, subject)[2]
                )
            )
        )

        val lesson2 = QuestLesson(
            id = "seed_${track.id}_${subject.lowercase().replace(" ", "_")}_2",
            trackId = track.id,
            subject = subject,
            title = if (isGovt) "Speed & Elimination Drill" else "$subject: Problem Solving",
            description = "Step-by-step practice solving exam-style $subject problems without memorizing blindly.",
            durationMinutes = 8,
            xpReward = 60,
            orderIndex = 1,
            concept3dId = linked3d,
            steps = listOf(
                LessonStep(
                    title = "Pattern Recognition",
                    owlPrompt = "In **$subject**, 80% of questions test 20% of core patterns. Look for invariants—quantities that stay conserved or balanced.",
                    keyFormulaOrFact = "Check extremes: test with 0, 1, or 100 to verify relationships quickly.",
                    checkpointQuestion = buildDefaultQuestions(track, subject)[3]
                ),
                LessonStep(
                    title = "Final Quest Check",
                    owlPrompt = "Final checkpoint for this unit! Take a breath and pick the strongest answer.",
                    keyFormulaOrFact = "Earn +60 XP and unlock offline revision cards on completion.",
                    checkpointQuestion = buildDefaultQuestions(track, subject)[4]
                )
            )
        )

        val lesson3 = QuestLesson(
            id = "seed_${track.id}_${subject.lowercase().replace(" ", "_")}_3",
            trackId = track.id,
            subject = subject,
            title = "3D Concept & Revision Lab",
            description = "Deep dive with 3D spatial exploration and milestone review for $subject.",
            durationMinutes = 7,
            xpReward = 70,
            orderIndex = 2,
            concept3dId = linked3d,
            steps = listOf(
                LessonStep(
                    title = "Spatial Mental Models",
                    owlPrompt = "Seeing a structure in 3D helps you recall relationships instantly during exams. Tap **Explore in 3D** after finishing this check!",
                    keyFormulaOrFact = "Visual memory recalls diagrams 2x faster than rote paragraphs.",
                    checkpointQuestion = mixedVisualQuestions[0]
                ),
                LessonStep(
                    title = "Milestone Checkpoint",
                    owlPrompt = "Great work! Complete this final question to earn bonus gems.",
                    keyFormulaOrFact = "Consistency beats cramming: 20 minutes daily builds unstoppable momentum.",
                    checkpointQuestion = mixedVisualQuestions[1]
                )
            )
        )

        return listOf(lesson1, lesson2, lesson3)
    }

    private fun buildDefaultQuestions(track: LearningTrack, subject: String): List<QuizQuestion> {
        return when {
            subject.contains("Math", true) || subject.contains("Quant", true) || subject.contains("CSAT", true) -> listOf(
                QuizQuestion("mq1", "What does the denominator in the fraction 3/4 tell us?", listOf("The whole number", "The color", "How many equal parts the whole is divided into", "The score"), 2, "The denominator (bottom number) shows how many equal parts make up one whole."),
                QuizQuestion("mq2", "If 25% of a number is 30, what is the full number?", listOf("90", "120", "150", "75"), 1, "25% is 1/4 of the whole, so 30 × 4 = 120."),
                QuizQuestion("mq3", "The sum of the interior angles of any triangle is always:", listOf("90°", "180°", "270°", "360°"), 1, "In Euclidean geometry, a triangle's three interior angles always add up to 180°."),
                QuizQuestion("mq4", "A train travels 180 km in 3 hours at constant speed. What is its speed?", listOf("45 km/h", "50 km/h", "60 km/h", "90 km/h"), 2, "Speed = Distance / Time = 180 / 3 = 60 km/h."),
                QuizQuestion("mq5", "Which expression represents the volume of a cylinder of radius r and height h?", listOf("2πrh", "πr²h", "4/3 πr³", "1/3 πr²h"), 1, "A cylinder stacks circular bases of area πr² to height h, giving Volume = πr²h.")
            )
            subject.contains("Polity", true) || subject.contains("Civics", true) || subject.contains("General Awareness", true) -> listOf(
                QuizQuestion("pq1", "Which part of the Indian Constitution guarantees Fundamental Rights?", listOf("Part I", "Part III", "Part IV", "Part IX"), 1, "Part III (Articles 12 to 35) enshrines Fundamental Rights."),
                QuizQuestion("pq2", "Which Article is called the 'Heart and Soul of the Constitution' by Dr. B.R. Ambedkar?", listOf("Article 14", "Article 19", "Article 21", "Article 32"), 3, "Article 32 grants the Right to Constitutional Remedies via writs."),
                QuizQuestion("pq3", "Money Bills in the Indian Parliament can be introduced only in:", listOf("Rajya Sabha", "Lok Sabha", "Joint Sitting", "State Council"), 1, "Under Article 110 & 109, a Money Bill can only be introduced in the Lok Sabha with the President's prior recommendation."),
                QuizQuestion("pq4", "Who is the ex-officio Chairman of the Rajya Sabha?", listOf("The Prime Minister", "The Speaker", "The Vice President of India", "The Chief Justice"), 2, "Under Article 64, the Vice President of India is the ex-officio Chairman of the Rajya Sabha."),
                QuizQuestion("pq5", "The minimum age to contest Lok Sabha elections in India is:", listOf("18 years", "21 years", "25 years", "30 years"), 2, "A candidate must be at least 25 years old for Lok Sabha (30 years for Rajya Sabha).")
            )
            subject.contains("Chem", true) -> listOf(
                QuizQuestion("cq1", "Which subatomic particle determines the atomic number and identity of an element?", listOf("Neutron", "Proton", "Valence electron", "Photon"), 1, "Atomic number Z equals the number of protons in the nucleus."),
                QuizQuestion("cq2", "What is the maximum number of electrons in the L-shell (n = 2)?", listOf("2", "8", "18", "32"), 1, "Using 2n², for n = 2: 2 × 2² = 8 electrons."),
                QuizQuestion("cq3", "A solution with pH = 2 is:", listOf("Strongly basic", "Neutral", "Strongly acidic", "Weakly basic"), 2, "pH below 7 is acidic; pH 2 indicates a high H⁺ ion concentration (strong acid)."),
                QuizQuestion("cq4", "Covalent bonds are formed by:", listOf("Transfer of electrons", "Sharing of electron pairs", "Loss of protons", "Nuclear fusion"), 1, "Non-metals share valence electron pairs to achieve stable octets."),
                QuizQuestion("cq5", "Which gas turns limewater milky?", listOf("Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"), 2, "CO₂ reacts with Ca(OH)₂ to form insoluble white CaCO₃ precipitate.")
            )
            subject.contains("Bio", true) || subject.contains("EVS", true) -> listOf(
                QuizQuestion("bq1", "Which organelle is known as the powerhouse of the cell because it generates ATP?", listOf("Golgi body", "Mitochondria", "Ribosome", "Lysosome"), 1, "Mitochondria perform aerobic cellular respiration to synthesize ATP."),
                QuizQuestion("bq2", "In DNA, Adenine (A) always pairs with:", listOf("Cytosine (C)", "Guanine (G)", "Thymine (T)", "Uracil (U)"), 2, "In double-stranded DNA, A pairs with T via 2 hydrogen bonds, and G pairs with C via 3."),
                QuizQuestion("bq3", "Photosynthesis takes place inside which plant cell organelle?", listOf("Chloroplast", "Vacuole", "Centrosome", "Nucleolus"), 0, "Chloroplasts contain chlorophyll to capture light energy and convert CO₂ + H₂O into glucose."),
                QuizQuestion("bq4", "Which blood vessels carry blood away from the heart?", listOf("Veins", "Arteries", "Capillaries", "Lymphatics"), 1, "Arteries carry blood away from the heart (mostly oxygenated, except the pulmonary artery)."),
                QuizQuestion("bq5", "Nitrogen fixation in leguminous roots is primarily performed by:", listOf("Yeast", "Rhizobium bacteria", "Amoeba", "Penicillium"), 1, "Rhizobium lives symbiotically in root nodules and converts atmospheric N₂ into usable nitrates.")
            )
            else -> mixedVisualQuestions
        }
    }

    private val mixedVisualQuestions = listOf(
        QuizQuestion(
            id = "vis_1",
            prompt = "In Bohr's atomic model, which particles reside inside the dense central nucleus?",
            options = listOf("Electrons only", "Protons and Neutrons", "Protons and Electrons", "Photons and Ions"),
            correctIndex = 1,
            explanation = "Protons (+1 charge) and Neutrons (0 charge) form the nucleus, while negatively charged electrons orbit in shells."
        ),
        QuizQuestion(
            id = "vis_2",
            prompt = "Which layer of the Earth generates our protective magnetic field via convective liquid iron?",
            options = listOf("Continental Crust", "Lithosphere", "Outer Core", "Troposphere"),
            correctIndex = 2,
            explanation = "Swirling molten iron-nickel in the Outer Core creates the geodynamo that powers Earth's magnetosphere."
        ),
        QuizQuestion(
            id = "vis_3",
            prompt = "In an electromagnetic wave, the Electric (E) and Magnetic (B) fields oscillate:",
            options = listOf("Parallel to each other", "Perpendicular to each other and the wave direction", "Only inside metals", "At random angles"),
            correctIndex = 1,
            explanation = "EM waves are transverse: E-field, B-field, and propagation direction are mutually orthogonal (90°)."
        ),
        QuizQuestion(
            id = "vis_4",
            prompt = "Why does a Frustum (truncated cone) have two circular faces?",
            options = listOf("A plane slices parallel to the cone's base", "It is made of two spheres", "Its slant height is zero", "It has no curved surface"),
            correctIndex = 0,
            explanation = "Slicing a right circular cone parallel to its base creates a smaller top circle and larger bottom circle."
        ),
        QuizQuestion(
            id = "vis_5",
            prompt = "What holds the two complementary strands of the DNA double helix together?",
            options = listOf("Ionic bonds", "Hydrogen bonds between nitrogenous bases", "Magnetic attraction", "Peptide bonds"),
            correctIndex = 1,
            explanation = "Weak hydrogen bonds between A-T and C-G allow DNA to unzip smoothly during replication."
        )
    )

    private val seedLessons: List<QuestLesson> = listOf(
        QuestLesson(
            id = "math_fractions_real_life",
            trackId = "class_5",
            subject = "Mathematics",
            title = "Fractions in real life",
            description = "See fractions as equal parts, then use them to solve everyday problems.",
            durationMinutes = 8,
            xpReward = 50,
            orderIndex = 1,
            concept3dId = "geometry_solids",
            steps = listOf(
                LessonStep(
                    title = "Parts of a Whole",
                    owlPrompt = "A fraction shows **part of a whole**. Imagine slicing a pizza into 4 equal slices and eating 3 of them — that's **3/4**!",
                    keyFormulaOrFact = "Fraction = Numerator (parts taken) / Denominator (equal parts in whole)",
                    checkpointQuestion = QuizQuestion(
                        id = "frac_1",
                        prompt = "If you split a chocolate bar into 8 equal squares and share 5, what fraction did you share?",
                        options = listOf("8/5", "5/8", "3/8", "5/3"),
                        correctIndex = 1,
                        explanation = "5 parts shared out of 8 total equal parts equals 5/8."
                    )
                ),
                LessonStep(
                    title = "Checkpoint Quiz",
                    owlPrompt = "Let's verify what each part of a fraction represents before advancing!",
                    keyFormulaOrFact = "Denominator sits downstairs (D = Down = Divided parts).",
                    checkpointQuestion = QuizQuestion(
                        id = "frac_2",
                        prompt = "What does the denominator tell us?",
                        options = listOf("The whole number", "The color", "How many equal parts", "The score"),
                        correctIndex = 2,
                        explanation = "The denominator tells us how many equal parts the whole is divided into."
                    )
                )
            )
        ),
        QuestLesson(
            id = "math_number_sense",
            trackId = "class_5",
            subject = "Mathematics",
            title = "Number sense",
            description = "Place value, comparing large numbers, and mental estimation.",
            durationMinutes = 6,
            xpReward = 40,
            orderIndex = 0,
            concept3dId = "geometry_solids",
            steps = listOf(
                LessonStep(
                    title = "Place Value Powers",
                    owlPrompt = "Each step left in our base-10 system multiplies value by **10**!",
                    keyFormulaOrFact = "10 Ones = 1 Ten | 10 Tens = 1 Hundred | 10 Hundreds = 1 Thousand",
                    checkpointQuestion = QuizQuestion(
                        id = "ns_1",
                        prompt = "What is the place value of 7 in 47,290?",
                        options = listOf("70", "700", "7,000", "70,000"),
                        correctIndex = 2,
                        explanation = "The digit 7 sits in the thousands place, so its value is 7,000."
                    )
                )
            )
        ),
        QuestLesson(
            id = "math_decimals_money",
            trackId = "class_5",
            subject = "Mathematics",
            title = "Decimals & money",
            description = "Connect tenths and hundredths directly to coins, bills, and bills.",
            durationMinutes = 7,
            xpReward = 55,
            orderIndex = 2,
            concept3dId = "geometry_solids",
            steps = listOf(
                LessonStep(
                    title = "Hundredths in Currency",
                    owlPrompt = "Decimals are just fractions with denominators of **10, 100, 1000**! One paisa or cent is **0.01** of a unit.",
                    keyFormulaOrFact = "0.25 = 25/100 = 1/4",
                    checkpointQuestion = QuizQuestion(
                        id = "dec_1",
                        prompt = "Which decimal is equal to 3/4?",
                        options = listOf("0.34", "0.75", "0.25", "0.50"),
                        correctIndex = 1,
                        explanation = "3 ÷ 4 = 0.75 (75 hundredths)."
                    )
                )
            )
        ),
        QuestLesson(
            id = "sci_10_chem_reactions",
            trackId = "class_10",
            subject = "Science",
            title = "Atomic Orbitals & Bonding",
            description = "Visualize electron shells in 3D and understand why atoms bond.",
            durationMinutes = 8,
            xpReward = 65,
            orderIndex = 0,
            concept3dId = "atom_bohr",
            steps = listOf(
                LessonStep(
                    title = "Valence Shell Stability",
                    owlPrompt = "Atoms want a full outer shell (usually **8 electrons** — the Octet Rule). Explore the 3D Bohr Atom to see shells K, L, and M!",
                    keyFormulaOrFact = "Shell Capacity = 2n² (K=2, L=8, M=18)",
                    checkpointQuestion = QuizQuestion(
                        id = "c10_1",
                        prompt = "Sodium (Z = 11) has electronic configuration 2, 8, 1. How does it achieve a stable octet?",
                        options = listOf("Gains 7 electrons", "Loses 1 valence electron to form Na⁺", "Splits its nucleus", "Becomes noble gas neon by adding protons"),
                        correctIndex = 1,
                        explanation = "Losing its single M-shell electron leaves a stable full L-shell of 8 electrons."
                    )
                ),
                LessonStep(
                    title = "Types of Chemical Bonds",
                    owlPrompt = "Transferring electrons makes **Ionic Bonds** (like NaCl), while sharing electrons makes **Covalent Bonds** (like H₂O and CH₄).",
                    keyFormulaOrFact = "Metal + Non-Metal = Ionic | Non-Metal + Non-Metal = Covalent",
                    checkpointQuestion = QuizQuestion(
                        id = "c10_2",
                        prompt = "Which molecule contains pure covalent bonds?",
                        options = listOf("NaCl", "MgO", "CH₄ (Methane)", "KBr"),
                        correctIndex = 2,
                        explanation = "Carbon and Hydrogen are both non-metals and share 4 electron pairs covalently."
                    )
                )
            )
        ),
        QuestLesson(
            id = "sci_10_heredity_dna",
            trackId = "class_10",
            subject = "Science",
            title = "DNA & Heredity Blueprint",
            description = "Rotate the 3D Double Helix and decode how traits pass across generations.",
            durationMinutes = 9,
            xpReward = 70,
            orderIndex = 1,
            concept3dId = "dna_helix",
            steps = listOf(
                LessonStep(
                    title = "The Twisted Ladder",
                    owlPrompt = "DNA's sugar-phosphate backbones twist into a **double helix**, connected by complementary rungs: **A-T** and **C-G**.",
                    keyFormulaOrFact = "Chargaff's Rule: %A = %T and %C = %G",
                    checkpointQuestion = QuizQuestion(
                        id = "dna_q1",
                        prompt = "If a DNA sample has 20% Adenine (A), what percentage is Guanine (G)?",
                        options = listOf("20%", "30%", "40%", "80%"),
                        correctIndex = 1,
                        explanation = "A = T = 20% (40% total). Remaining 60% is split equally between C and G, so G = 30%."
                    )
                )
            )
        )
    )

    private fun buildBohrAtomScene(): Concept3DScene {
        val nodes = mutableListOf(
            SceneNode3D("p1", "Proton (+)", "Positively charged baryon in the nucleus. Atomic number Z = count of protons.", PrimitiveShape3D.SPHERE, 0.14f, 0.1f, 0.05f, 0.22f, 0xFFE04F5FL, explodeVectorX = 0.25f, explodeVectorY = 0.15f),
            SceneNode3D("p2", "Proton (+)", "Repels other protons electromagnetically, held tight by the Strong Nuclear Force.", PrimitiveShape3D.SPHERE, -0.14f, -0.08f, -0.08f, 0.22f, 0xFFE04F5FL, explodeVectorX = -0.25f, explodeVectorY = -0.15f),
            SceneNode3D("n1", "Neutron (0)", "Electrically neutral nucleon stabilizing the nucleus against proton-proton repulsion.", PrimitiveShape3D.SPHERE, -0.1f, 0.14f, 0.1f, 0.22f, 0xFF616B63L, explodeVectorZ = 0.28f),
            SceneNode3D("n2", "Neutron (0)", "Contributes to atomic mass number A = Protons + Neutrons.", PrimitiveShape3D.SPHERE, 0.12f, -0.12f, -0.1f, 0.22f, 0xFF616B63L, explodeVectorZ = -0.28f),
            SceneNode3D("k_ring", "K-Shell (n=1)", "Innermost shell holding up to 2 electrons (2n² = 2).", PrimitiveShape3D.RING, 0f, 0f, 0f, 0.95f, 0xFF28824BL, orbitTiltDeg = 22f),
            SceneNode3D("l_ring", "L-Shell (n=2)", "Second energy level holding up to 8 valence/core electrons.", PrimitiveShape3D.RING, 0f, 0f, 0f, 1.55f, 0xFF2B8AE2L, orbitTiltDeg = -32f),
            SceneNode3D("m_ring", "M-Shell (n=3)", "Outer valence shell where chemical bonding reactions occur.", PrimitiveShape3D.RING, 0f, 0f, 0f, 2.05f, 0xFFEA7E1EL, orbitTiltDeg = 55f),
            SceneNode3D("e1", "Electron e⁻ (K)", "High-speed negative lepton occupying the lowest energy K-shell.", PrimitiveShape3D.SPHERE, 0.95f, 0f, 0f, 0.11f, 0xFF2B8AE2L, orbitSpeed = 2.4f, orbitRadius = 0.95f, orbitTiltDeg = 22f),
            SceneNode3D("e2", "Electron e⁻ (L)", "Orbits in the L-shell; jumps outward when absorbing a photon.", PrimitiveShape3D.SPHERE, -1.55f, 0f, 0f, 0.11f, 0xFF2B8AE2L, orbitSpeed = -1.6f, orbitRadius = 1.55f, orbitTiltDeg = -32f),
            SceneNode3D("e3", "Valence Electron", "Outermost electron shared or transferred during ionic/covalent bonding.", PrimitiveShape3D.SPHERE, 0f, 2.05f, 0f, 0.13f, 0xFFEA7E1EL, orbitSpeed = 1.1f, orbitRadius = 2.05f, orbitTiltDeg = 55f)
        )
        return Concept3DScene(
            id = "atom_bohr",
            title = "Bohr & Quantum Atomic Model",
            subject = "Chemistry / Physics",
            levelLabel = "Class 8–12 & GS",
            owlSummary = "Atoms are 99.99% empty space! A tiny dense nucleus of protons and neutrons sits at the center while quantized electron shells orbit at fixed energy levels.",
            keyTakeaways = listOf(
                "Nucleus contains Protons (+) and Neutrons (0)",
                "Shell capacity follows 2n² (2, 8, 18, 32)",
                "Outermost valence electrons drive all chemical reactions"
            ),
            nodes = nodes
        )
    }

    private fun buildDnaHelixScene(): Concept3DScene {
        val nodes = mutableListOf<SceneNode3D>()
        val steps = 12
        for (i in 0 until steps) {
            val t = (i / (steps - 1f)) * 2f - 1f
            val y = t * 1.95f
            val angle = i * (PI / 3.2).toFloat()
            val r = 0.85f
            val x1 = cos(angle) * r
            val z1 = sin(angle) * r
            val x2 = -x1
            val z2 = -z1
            val isAt = i % 2 == 0
            nodes.add(
                SceneNode3D(
                    id = "bp1_$i",
                    label = if (isAt) "Adenine (A) + Backbone" else "Cytosine (C) + Backbone",
                    detail = if (isAt) "Sugar-phosphate backbone linked to Adenine (pairs only with Thymine)." else "Sugar-phosphate backbone linked to Cytosine (pairs only with Guanine).",
                    shape = PrimitiveShape3D.SPHERE,
                    x = x1, y = y, z = z1,
                    radiusOrSize = 0.14f,
                    colorHex = if (isAt) 0xFF28824BL else 0xFF2B8AE2L,
                    explodeVectorX = x1 * 0.45f,
                    explodeVectorZ = z1 * 0.45f
                )
            )
            nodes.add(
                SceneNode3D(
                    id = "bp2_$i",
                    label = if (isAt) "Thymine (T) + Backbone" else "Guanine (G) + Backbone",
                    detail = if (isAt) "Thymine forms 2 hydrogen bonds with Adenine." else "Guanine forms 3 hydrogen bonds with Cytosine.",
                    shape = PrimitiveShape3D.SPHERE,
                    x = x2, y = y, z = z2,
                    radiusOrSize = 0.14f,
                    colorHex = if (isAt) 0xFFEA7E1EL else 0xFFE04F5FL,
                    explodeVectorX = x2 * 0.45f,
                    explodeVectorZ = z2 * 0.45f
                )
            )
            nodes.add(
                SceneNode3D(
                    id = "rung_$i",
                    label = if (isAt) "A=T Hydrogen Rung" else "C≡G Hydrogen Rung",
                    detail = if (isAt) "2 hydrogen bonds link A and T across the helix." else "3 hydrogen bonds link C and G across the helix.",
                    shape = PrimitiveShape3D.ROD,
                    x = x1, y = y, z = z1,
                    radiusOrSize = 0.055f,
                    colorHex = if (isAt) 0xFF8FBFA0L else 0xFFA5C9E8L,
                    endX = x2, endY = y, endZ = z2
                )
            )
        }
        return Concept3DScene(
            id = "dna_helix",
            title = "DNA Double Helix",
            subject = "Biology",
            levelLabel = "Class 9–12 & NEET/GS",
            owlSummary = "DNA stores genetic instructions in two antiparallel strands twisted around a central axis. Complementary base pairing (A-T, C-G) enables exact copying during cell division.",
            keyTakeaways = listOf(
                "Two antiparallel sugar-phosphate backbones",
                "Purines (A, G) always pair with Pyrimidines (T, C)",
                "Explode layers to inspect individual base rungs!"
            ),
            nodes = nodes
        )
    }

    private fun buildSolarSystemScene(): Concept3DScene {
        val nodes = listOf(
            SceneNode3D("sun", "The Sun (G-Type Star)", "Contains 99.86% of the Solar System's mass; fuses Hydrogen into Helium.", PrimitiveShape3D.SPHERE, 0f, 0f, 0f, 0.42f, 0xFFEA7E1EL),
            SceneNode3D("orb_mercury", "Mercury Orbit", "Closest rocky planet; 88 Earth-day year.", PrimitiveShape3D.RING, 0f, 0f, 0f, 0.72f, 0xFF9EA6A0L, orbitTiltDeg = 8f),
            SceneNode3D("mercury", "Mercury", "Smallest planet with extreme day/night temperatures and no atmosphere.", PrimitiveShape3D.SPHERE, 0.72f, 0f, 0f, 0.08f, 0xFF8C8C8CL, orbitSpeed = 2.6f, orbitRadius = 0.72f, orbitTiltDeg = 8f),
            SceneNode3D("orb_earth", "Earth Orbit (1 AU)", "Goldilocks habitable zone where liquid water persists.", PrimitiveShape3D.RING, 0f, 0f, 0f, 1.18f, 0xFF2B8AE2L, orbitTiltDeg = 12f),
            SceneNode3D("earth", "Earth", "Third rock from the Sun; tilted 23.5° giving us seasons.", PrimitiveShape3D.SPHERE, -1.18f, 0f, 0f, 0.14f, 0xFF2B8AE2L, orbitSpeed = 1.4f, orbitRadius = 1.18f, orbitTiltDeg = 12f),
            SceneNode3D("orb_mars", "Mars Orbit", "The Red Planet — iron oxide dust and Olympus Mons.", PrimitiveShape3D.RING, 0f, 0f, 0f, 1.56f, 0xFFE04F5FL, orbitTiltDeg = 15f),
            SceneNode3D("mars", "Mars", "Has polar ice caps and two small moons, Phobos and Deimos.", PrimitiveShape3D.SPHERE, 0f, 0f, 1.56f, 0.11f, 0xFFE04F5FL, orbitSpeed = 1.0f, orbitRadius = 1.56f, orbitTiltDeg = 15f),
            SceneNode3D("orb_saturn", "Saturn & Rings", "Gas giant famous for icy ring system; less dense than water.", PrimitiveShape3D.RING, 0f, 0f, 0f, 2.08f, 0xFFD4A346L, orbitTiltDeg = 20f),
            SceneNode3D("saturn", "Saturn", "Second largest planet; Titan is its methane-rich largest moon.", PrimitiveShape3D.SPHERE, 2.08f, 0f, 0f, 0.21f, 0xFFD4A346L, orbitSpeed = 0.6f, orbitRadius = 2.08f, orbitTiltDeg = 20f)
        )
        return Concept3DScene(
            id = "solar_system",
            title = "Solar System & Kepler Orbits",
            subject = "Geography / Astronomy",
            levelLabel = "Class 3–11 & UPSC",
            owlSummary = "Planets revolve around the Sun in elliptical orbits under gravity. Inner rocky planets orbit much faster than outer gas giants (Kepler's 3rd Law).",
            keyTakeaways = listOf(
                "Inner planets (Mercury–Mars) are rocky terrestrial worlds",
                "Earth's 23.5° axial tilt causes seasons",
                "Orbital velocity decreases with distance from the Sun"
            ),
            nodes = nodes
        )
    }

    private fun buildGeometrySolidsScene(): Concept3DScene {
        val nodes = listOf(
            SceneNode3D("cube", "Unit Cube (a³)", "6 square faces, 12 edges, 8 vertices. Volume = a³, Total Surface Area = 6a².", PrimitiveShape3D.CUBE, -1.1f, 0.1f, 0f, 0.65f, 0xFF28824BL, explodeVectorX = -0.5f),
            SceneNode3D("cone", "Right Circular Cone", "Volume = 1/3 πr²h — exactly one-third of a cylinder with equal base and height!", PrimitiveShape3D.CONE, 0.1f, 0.1f, 0.25f, 0.75f, 0xFFEA7E1EL, explodeVectorY = 0.45f),
            SceneNode3D("sphere", "Sphere (4/3 πr³)", "Every surface point is equidistant (r) from center. Surface Area = 4πr².", PrimitiveShape3D.SPHERE, 1.25f, 0.1f, -0.15f, 0.45f, 0xFF2B8AE2L, explodeVectorX = 0.5f),
            SceneNode3D("base_ring", "Equatorial Cross-Section", "Slicing a sphere through its center produces the Great Circle of area πr².", PrimitiveShape3D.RING, 1.25f, 0.1f, -0.15f, 0.55f, 0xFF1A5E35L, explodeVectorX = 0.5f)
        )
        return Concept3DScene(
            id = "geometry_solids",
            title = "3D Mensuration & Solids",
            subject = "Mathematics",
            levelLabel = "Class 5–12 & SSC/Quant",
            owlSummary = "Compare Cube, Cone, and Sphere side by side in 3D space! Drag to inspect vertices, curved surfaces, and cross-sectional symmetry.",
            keyTakeaways = listOf(
                "Cone Volume = 1/3 × Cylinder Volume",
                "Sphere Surface Area = 4 × Great Circle Area (4πr²)",
                "Cube diagonal = a√3"
            ),
            nodes = nodes
        )
    }

    private fun buildEarthLayersScene(): Concept3DScene {
        val nodes = listOf(
            SceneNode3D("inner_core", "Inner Core (Solid Fe-Ni)", "5,400°C solid iron-nickel alloy kept solid by immense pressure.", PrimitiveShape3D.SPHERE, 0f, 0f, 0f, 0.42f, 0xFFFFD166L),
            SceneNode3D("outer_core", "Outer Core (Liquid Metal)", "Convecting molten iron generating Earth's protective magnetic field.", PrimitiveShape3D.SPHERE, 0f, -0.35f, 0.45f, 0.62f, 0xFFEA7E1EL, explodeVectorY = -0.4f, explodeVectorZ = 0.4f),
            SceneNode3D("mantle", "Mantle (Silicate Magma)", "2,900 km thick convecting rock driving tectonic plate movement.", PrimitiveShape3D.SPHERE, 0.55f, 0.35f, -0.25f, 0.76f, 0xFFE04F5FL, explodeVectorX = 0.55f, explodeVectorY = 0.3f),
            SceneNode3D("crust", "Crust & Lithosphere", "Thin oceanic (basalt) & continental (granite) plates where we live.", PrimitiveShape3D.RING, 0f, 0f, 0f, 1.72f, 0xFF28824BL, orbitTiltDeg = 18f),
            SceneNode3D("atmo", "Atmosphere Shell", "Troposphere to Exosphere — shields life from UV & retains warmth.", PrimitiveShape3D.RING, 0f, 0f, 0f, 2.02f, 0xFF2B8AE2L, orbitTiltDeg = -28f)
        )
        return Concept3DScene(
            id = "earth_layers",
            title = "Earth's Interior & Tectonics",
            subject = "Geography",
            levelLabel = "Class 6–12 & UPSC",
            owlSummary = "Use the Explode slider to peel back Earth's Crust, Mantle, Liquid Outer Core, and Solid Inner Core!",
            keyTakeaways = listOf(
                "Mohorovicic discontinuity separates Crust & Mantle",
                "Liquid Outer Core blocks S-waves (seismic shadow zone)",
                "Mantle convection drives continental drift & earthquakes"
            ),
            nodes = nodes
        )
    }

    private fun buildPlantCellScene(): Concept3DScene {
        val nodes = listOf(
            SceneNode3D("wall", "Cellulose Cell Wall", "Rigid outer boundary giving plant cells structural support.", PrimitiveShape3D.CUBE, 0f, 0f, 0f, 1.65f, 0xFF28824BL, explodeVectorY = -0.3f),
            SceneNode3D("nucleus", "Nucleus & Nucleolus", "Control center housing DNA chromosomes and ribosome assembly.", PrimitiveShape3D.SPHERE, -0.45f, 0.25f, 0.2f, 0.42f, 0xFF6B4FAAL, explodeVectorX = -0.45f),
            SceneNode3D("vacuole", "Central Vacuole", "Large water & turgor pressure reservoir occupying up to 80% of volume.", PrimitiveShape3D.SPHERE, 0.45f, 0.05f, 0.15f, 0.52f, 0xFF2B8AE2L, explodeVectorX = 0.45f),
            SceneNode3D("chloro1", "Chloroplast", "Site of photosynthesis converting sunlight + CO₂ + H₂O into glucose.", PrimitiveShape3D.SPHERE, -0.35f, -0.45f, 0.45f, 0.24f, 0xFF3EA365L, explodeVectorZ = 0.45f),
            SceneNode3D("mito1", "Mitochondrion", "Cellular powerhouse producing ATP energy molecules.", PrimitiveShape3D.SPHERE, 0.2f, -0.45f, -0.45f, 0.22f, 0xFFEA7E1EL, explodeVectorZ = -0.45f)
        )
        return Concept3DScene(
            id = "plant_cell",
            title = "Eukaryotic Plant Cell",
            subject = "Biology",
            levelLabel = "Class 8–12",
            owlSummary = "Inspect how plant cells combine a rigid cellulose wall, central vacuole, chloroplasts, and mitochondria in 3D!",
            keyTakeaways = listOf(
                "Cell wall + Chloroplasts + Large Vacuole distinguish plants from animal cells",
                "Mitochondria & Chloroplasts both possess their own DNA",
                "Turgor pressure keeps stems upright"
            ),
            nodes = nodes
        )
    }

    private fun buildElectromagneticWaveScene(): Concept3DScene {
        val nodes = mutableListOf<SceneNode3D>()
        val count = 13
        for (i in 0 until count) {
            val x = -1.8f + (i / (count - 1f)) * 3.6f
            val phase = (i / (count - 1f)) * 2f * PI.toFloat()
            val amp = 0.95f * sin(phase)
            // E-field vertical (Y)
            nodes.add(
                SceneNode3D(
                    id = "e_$i",
                    label = "Electric Field Vector (E)",
                    detail = "Oscillates vertically along the Y-axis perpendicular to propagation.",
                    shape = PrimitiveShape3D.ROD,
                    x = x, y = 0f, z = 0f,
                    radiusOrSize = 0.05f,
                    colorHex = 0xFFE04F5FL,
                    endX = x, endY = amp, endZ = 0f
                )
            )
            nodes.add(
                SceneNode3D(
                    id = "etip_$i",
                    label = "E-Wave Crest/Trough",
                    detail = "Peak electric field strength E₀.",
                    shape = PrimitiveShape3D.SPHERE,
                    x = x, y = amp, z = 0f,
                    radiusOrSize = 0.09f,
                    colorHex = 0xFFE04F5FL
                )
            )
            // B-field horizontal (Z)
            nodes.add(
                SceneNode3D(
                    id = "b_$i",
                    label = "Magnetic Field Vector (B)",
                    detail = "Oscillates along Z-axis at 90° to the Electric field.",
                    shape = PrimitiveShape3D.ROD,
                    x = x, y = 0f, z = 0f,
                    radiusOrSize = 0.05f,
                    colorHex = 0xFF2B8AE2L,
                    endX = x, endY = 0f, endZ = amp,
                    explodeVectorZ = amp * 0.35f
                )
            )
            nodes.add(
                SceneNode3D(
                    id = "btip_$i",
                    label = "B-Wave Crest/Trough",
                    detail = "Peak magnetic flux density B₀ = E₀ / c.",
                    shape = PrimitiveShape3D.SPHERE,
                    x = x, y = 0f, z = amp,
                    radiusOrSize = 0.09f,
                    colorHex = 0xFF2B8AE2L,
                    explodeVectorZ = amp * 0.35f
                )
            )
        }
        nodes.add(
            SceneNode3D(
                id = "axis",
                label = "Wave Propagation Axis (c)",
                detail = "Light travels at c = 3 × 10⁸ m/s perpendicular to both E and B.",
                shape = PrimitiveShape3D.ROD,
                x = -1.95f, y = 0f, z = 0f,
                radiusOrSize = 0.045f,
                colorHex = 0xFF28824BL,
                endX = 1.95f, endY = 0f, endZ = 0f
            )
        )
        return Concept3DScene(
            id = "em_wave",
            title = "Electromagnetic Wave 3D",
            subject = "Physics",
            levelLabel = "Class 10–12 & JEE/NDA",
            owlSummary = "Light is a self-propagating dance between an Electric field (Red, vertical) and a Magnetic field (Blue, horizontal) moving at speed c!",
            keyTakeaways = listOf(
                "E ⊥ B ⊥ Direction of Propagation (Transverse wave)",
                "Speed c = f × λ (frequency × wavelength)",
                "Requires no medium — travels through vacuum"
            ),
            nodes = nodes
        )
    }
}
