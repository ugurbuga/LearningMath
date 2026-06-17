package com.ugurbuga.learningmath.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ugurbuga.learningmath.model.Operation
import com.ugurbuga.learningmath.ui.components.StatsDashboard
import com.ugurbuga.learningmath.ui.theme.LearningMathTheme
import com.ugurbuga.learningmath.res.Strings
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SelectionScreen(
    onOperationSelected: (Operation) -> Unit,
    onStatsDetailClick: () -> Unit,
    stats: Map<String, Map<Operation, Int>>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        LearningMathLogo()

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = Strings.APP_NAME,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        StatsDashboard(
            stats = stats,
            onDetailClick = onStatsDetailClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(Operation.entries) { operation ->
                OperationCard(operation = operation, onClick = { onOperationSelected(operation) })
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OperationCard(operation: Operation, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Card(
        modifier = Modifier
            .aspectRatio(1.2f)
            .clip(shape)
            .clickable { onClick() },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = operation.color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = operation.symbol,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = operation.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun LearningMathLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.size(80.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LogoSymbol(Operation.ADDITION, Modifier.weight(1f).fillMaxHeight())
            LogoSymbol(Operation.SUBTRACTION, Modifier.weight(1f).fillMaxHeight())
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LogoSymbol(Operation.MULTIPLICATION, Modifier.weight(1f).fillMaxHeight())
            LogoSymbol(Operation.DIVISION, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun LogoSymbol(operation: Operation, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(operation.color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = operation.symbol,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun SelectionScreenPreview() {
    LearningMathTheme {
        SelectionScreen({}, {}, emptyMap())
    }
}
