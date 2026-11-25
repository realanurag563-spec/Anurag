package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.parseRichBoldText
import com.example.data.curriculum.CurriculumCatalog
import com.example.model.DoubtMessageDoc
import com.example.model.DoubtSessionDoc
import com.example.model.ExpertTutorProfile
import com.example.model.OwlChatMessage
import com.example.model.UiState
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.OwlMood
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft

@Composable
fun AskAndTutorsScreen(
    owlMessages: List<OwlChatMessage>,
    isAskingOwl: Boolean,
    owlError: String?,
    doubtSessionsState: UiState<List<DoubtSessionDoc>>,
    activeSessionId: String?,
    activeMessagesState: UiState<List<DoubtMessageDoc>>,
    tutorLoading: Boolean,
    tutorError: String?,
    trackTitle: String,
    subject: String,
    isOnline: Boolean,
    onAskOwl: (String) -> Unit,
    onOpen3DWithTopic: (String) -> Unit,
    onStartTutorSession: (ExpertTutorProfile, String, String, String) -> Unit,
    onOpenSession: (String) -> Unit,
    onCloseSession: () -> Unit,
    onSendStudentMessage: (DoubtSessionDoc, String) -> Unit,
    onRequestMentorAssist: (DoubtSessionDoc, String) -> Unit,
    onQueueLiveTutor: (String) -> Unit,
    onResolveSession: (String) -> Unit,
    onClearErrors: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Owl Desk, 1 = Expert Tutors

    val sessions = (doubtSessionsState as? UiState.Success)?.data.orEmpty()
    val activeSession = sessions.firstOrNull { it.id == activeSessionId }

    if (selectedSubTab == 1 && activeSession != null) {
        BackHandler { onCloseSession() }
        LiveDoubtRoomView(
            session = activeSession,
            messagesState = activeMessagesState,
            isLoading = tutorLoading,
            error = tutorError,
            onBack = onCloseSession,
            onSendMessage = { onSendStudentMessage(activeSession, it) },
            onRequestAssist = { onRequestMentorAssist(activeSession, it) },
            onQueueHuman = { onQueueLiveTutor(activeSession.id) },
            onResolve = { onResolveSession(activeSession.id) },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header matching Screenshot 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "OWL'S DOUBT DESK",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestEmerald
                    )
                    Text(
                        text = if (selectedSubTab == 0) "Ask anything" else "Expert Tutors",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                OnlineStatusBadge(isOnline = isOnline)
            }

            // Segmented pill toggle: Owl (Google Search) vs Expert Tutors
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ParchmentCard,
                border = BorderStroke(1.dp, ParchmentBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf("Owl (Google Research)", "Expert Tutors").forEachIndexed { idx, label ->
                        val active = selectedSubTab == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedSubTab = idx }
                                .testTag("ask_subtab_$idx"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) ForestEmerald else Color.Transparent
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (selectedSubTab == 0) {
            OwlChatTab(
                messages = owlMessages,
                isAsking = isAskingOwl,
                error = owlError,
                subject = subject,
                onSend = {
                    onClearErrors()
                    onAskOwl(it)
                },
                onOpen3DWithTopic = onOpen3DWithTopic,
                modifier = Modifier.weight(1f)
            )
        } else {
            ExpertTutorDirectoryTab(
                sessions = sessions,
                defaultSubject = subject,
                trackTitle = trackTitle,
                isOnline = isOnline,
                onOpenSession = onOpenSession,
                onStartSession = onStartTutorSession,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OwlChatTab(
    messages: List<OwlChatMessage>,
    isAsking: Boolean,
    error: String?,
    subject: String,
    onSend: (String) -> Unit,
    onOpen3DWithTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.fromOwl) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OwlMascotAvatar(
                            size = 42.dp,
                            mood = if (msg.isStreaming) OwlMood.THINKING else OwlMood.HAPPY
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MintSurface,
                            border = BorderStroke(1.dp, MintBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Owl",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ForestEmeraldDark
                                )
                                Text(
                                    text = parseRichBoldText(msg.text),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                if (msg.sources.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "GOOGLE RESEARCH SOURCES",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ForestEmerald
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            msg.sources.forEach { source ->
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = MaterialTheme.colorScheme.background,
                                                    border = BorderStroke(1.dp, MintBorder),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .clickable {
                                                            runCatching { uriHandler.openUri(source.url) }
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = source.title.take(26),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = ForestEmeraldDark,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Icon(
                                                            Icons.Filled.OpenInNew,
                                                            contentDescription = "Open source",
                                                            tint = ForestEmerald,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (msg.searchQueries.isNotEmpty()) {
                                    Text(
                                        text = "Researched: ${msg.searchQueries.joinToString(" • ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val topic3d = msg.suggested3dTopic
                                if (!msg.isStreaming && !topic3d.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = { onOpen3DWithTopic(topic3d) },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, ForestEmerald)
                                    ) {
                                        Icon(Icons.Filled.ViewInAr, null, tint = ForestEmerald, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Visualize in 3D", color = ForestEmeraldDark)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ForestEmerald,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            if (!error.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = WarmAmberSoft,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7B3E00),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Quick suggestion chips & bottom input bar matching Screenshot 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Explain photosynthesis",
                    "Revise fractions",
                    "UPSC Fundamental Rights",
                    "Bohr Atom model",
                    "Latest $subject exam tips"
                ).forEach { prompt ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ParchmentCard,
                        border = BorderStroke(1.dp, ParchmentBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !isAsking) { onSend(prompt) }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ParchmentCard,
                border = BorderStroke(1.dp, ParchmentBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type your doubt...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("owl_doubt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (inputText.isNotBlank() && !isAsking) ForestEmerald else Color(0xFF9EC3AC),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(enabled = inputText.isNotBlank() && !isAsking) {
                                val q = inputText
                                inputText = ""
                                onSend(q)
                            }
                            .testTag("owl_send_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isAsking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Filled.ArrowUpward,
                                    contentDescription = "Ask Owl",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpertTutorDirectoryTab(
    sessions: List<DoubtSessionDoc>,
    defaultSubject: String,
    trackTitle: String,
    isOnline: Boolean,
    onOpenSession: (String) -> Unit,
    onStartSession: (ExpertTutorProfile, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTutorForModal by remember { mutableStateOf<ExpertTutorProfile?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MintSurface,
                border = BorderStroke(1.dp, MintBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_tutor_banner),
                        contentDescription = "Expert live tutor connect banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Real-Time Doubt Connect",
                            style = MaterialTheme.typography.titleLarge,
                            color = ForestEmeraldDark
                        )
                        Text(
                            text = "Start a live Firestore doubt room with an expert tutor for $trackTitle ($defaultSubject). Get instant AI Mentor Assist immediately, plus queue a 1-on-1 live human slot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (sessions.isNotEmpty()) {
            item {
                Text(
                    text = "Your Doubt Threads",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(sessions, key = { it.id }) { session ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ParchmentCard,
                    border = BorderStroke(1.dp, ParchmentBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenSession(session.id) }
                        .testTag("doubt_session_${session.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Forum, null, tint = ForestEmerald, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${session.topic} • ${session.tutorName}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = session.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (session.status == "RESOLVED") MintSurface else WarmAmberSoft
                        ) {
                            Text(
                                text = session.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (session.status == "RESOLVED") ForestEmeraldDark else Color(0xFF7B3E00),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Subject Expert Tutors",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(CurriculumCatalog.tutors, key = { it.id }) { tutor ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ParchmentCard,
                border = BorderStroke(1.dp, ParchmentBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tutor.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = tutor.specialty,
                                style = MaterialTheme.typography.bodySmall,
                                color = ForestEmeraldDark
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (tutor.onlineNow) MintSurface else MaterialTheme.colorScheme.background
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Star, null, tint = WarmAmber, modifier = Modifier.size(13.dp))
                                Text("${tutor.rating} • ${tutor.availabilityLabel}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text(
                        text = tutor.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { selectedTutorForModal = tutor },
                        enabled = isOnline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ask_tutor_${tutor.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
                    ) {
                        Icon(Icons.Filled.SupportAgent, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOnline) "Ask Doubt in Live Room" else "Reconnect to Message Tutor")
                    }
                }
            }
        }
    }

    val targetTutor = selectedTutorForModal
    if (targetTutor != null) {
        StartTutorDoubtDialog(
            tutor = targetTutor,
            defaultSubject = defaultSubject,
            onDismiss = { selectedTutorForModal = null },
            onSubmit = { subj, topic, question ->
                selectedTutorForModal = null
                onStartSession(targetTutor, subj, topic, question)
            }
        )
    }
}

@Composable
private fun StartTutorDoubtDialog(
    tutor: ExpertTutorProfile,
    defaultSubject: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Column {
                Text("Ask ${tutor.name}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = tutor.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestEmeraldDark
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Chapter or Topic") },
                    placeholder = { Text("e.g., Quadratic Roots / Article 32") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Your exact doubt") },
                    placeholder = { Text("Describe where you got stuck...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(defaultSubject, topic.ifBlank { defaultSubject }, question) },
                enabled = question.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
            ) {
                Text("Start Live Doubt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun LiveDoubtRoomView(
    session: DoubtSessionDoc,
    messagesState: UiState<List<DoubtMessageDoc>>,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRequestAssist: (String) -> Unit,
    onQueueHuman: () -> Unit,
    onResolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }
    val messages = (messagesState as? UiState.Success)?.data.orEmpty()
    val isResolved = session.status == "RESOLVED"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(
            color = MintSurface,
            border = BorderStroke(1.dp, MintBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("doubt_room_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to tutors")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(session.tutorName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${session.topic} • ${session.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestEmeraldDark
                        )
                    }
                    if (!isResolved) {
                        TextButton(onClick = onResolve) {
                            Icon(Icons.Filled.CheckCircle, null, tint = ForestEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resolve", color = ForestEmeraldDark)
                        }
                    }
                }

                if (!isResolved) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val lastQ = messages.lastOrNull { it.senderRole == "STUDENT" }?.text ?: session.topic
                                onRequestAssist(lastQ)
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Psychology, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Assist", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onQueueHuman,
                            enabled = session.status != "QUEUED_HUMAN",
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (session.status == "QUEUED_HUMAN") "Queued 1-on-1" else "Queue 1-on-1",
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isStudent = msg.senderRole == "STUDENT"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isStudent) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isStudent) ForestEmerald else ParchmentCard,
                        border = if (isStudent) null else BorderStroke(1.dp, ParchmentBorder),
                        modifier = Modifier.widthIn(max = 290.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isStudent) Color(0xFFD5EBD9) else ForestEmeraldDark
                            )
                            Text(
                                text = parseRichBoldText(msg.text),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isStudent) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (!isResolved) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Reply to tutor...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                )
                Button(
                    onClick = {
                        onSendMessage(draft)
                        draft = ""
                    },
                    enabled = draft.isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}
