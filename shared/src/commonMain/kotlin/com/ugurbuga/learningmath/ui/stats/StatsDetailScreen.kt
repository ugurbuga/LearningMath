package com.ugurbuga.learningmath.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ugurbuga.learningmath.model.Operation
import com.ugurbuga.learningmath.util.getCurrentDate
import com.ugurbuga.learningmath.util.getDayOfWeekName
import com.ugurbuga.learningmath.util.getMonthName
import com.ugurbuga.learningmath.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDetailScreen(
    stats: Map<String, Map<Operation, Int>>,
    onBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalendarRow(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            val dayStats = stats[selectedDate.toString()] ?: emptyMap()
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Operation.entries.forEach { op ->
                    StatItem(operation = op, count = dayStats[op] ?: 0)
                }
            }
        }
    }
}

@Composable
fun StatItem(operation: Operation, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = operation.color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(operation.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(operation.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(operation.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = operation.color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.solutions_count),
                    style = MaterialTheme.typography.titleSmall,
                    color = operation.color.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CalendarRow(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val days = remember {
        val totalDays = mutableListOf<LocalDate>()
        val today = getCurrentDate()
        val start = today.minus(6, DateTimeUnit.MONTH)
        var current = start
        val end = today
        while (current <= end) {
            totalDays.add(current)
            current = current.plus(1, DateTimeUnit.DAY)
        }
        totalDays
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val today = getCurrentDate()
        val index = days.indexOf(today)
        if (index != -1) {
            listState.scrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(days) { date ->
            val isSelected = date == selectedDate
            Column(
                modifier = Modifier
                    .width(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getDayOfWeekName(date),
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getMonthName(date),
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
