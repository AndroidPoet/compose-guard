/*
 * ComposeGuard Experimental Rules Test Sample
 *
 * This file contains examples for each of the 4 experimental rules.
 * Open this file in Android Studio with ComposeGuard installed and
 * enable experimental rules in Settings to verify all rules are working.
 *
 * IMPORTANT: Experimental rules are disabled by default.
 * Enable them in: Settings > Tools > ComposeGuard > Experimental Rules
 *
 * Experimental Rules (4):
 * 1. LazyListMissingKey - items() should have key parameter
 * 2. LazyListContentType - heterogeneous items need contentType
 * 3. DerivedStateOfCandidate - computed values should use remember with keys
 * 4. FrequentRecomposition - suggest collectAsStateWithLifecycle for flows
 */

@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE")

package sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.StateFlow

// =============================================================================
// Data classes for examples
// =============================================================================

data class User(val id: String, val name: String, val email: String)
data class Item(val id: Int, val title: String, val category: String)

interface ViewModel {
    val users: StateFlow<List<User>>
    fun onUserClick(user: User)
}

// =============================================================================
// 1. LazyListMissingKey - items() should have key parameter
// =============================================================================

/**
 * VIOLATION: LazyColumn items() without key parameter
 *
 * Without a key, Compose cannot efficiently track item identity across
 * recompositions, leading to unnecessary recompositions and state loss.
 */
@Composable
fun LazyListMissingKey_Violation(users: List<User>) {
    LazyColumn {
        // WARNING: 'items' is missing a key parameter
        items(users) { user ->
            Text(user.name)
        }
    }
}

/**
 * VIOLATION: LazyRow itemsIndexed() without key parameter
 */
@Composable
fun LazyRowMissingKey_Violation(items: List<Item>) {
    LazyRow {
        // WARNING: 'itemsIndexed' is missing a key parameter
        itemsIndexed(items) { index, item ->
            Text("$index: ${item.title}")
        }
    }
}

/**
 * CORRECT: LazyColumn items() with key parameter
 */
@Composable
fun LazyListWithKey_Correct(users: List<User>) {
    LazyColumn {
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
    }
}

// =============================================================================
// 2. LazyListContentType - heterogeneous items need contentType
// =============================================================================

/**
 * VIOLATION: Heterogeneous LazyColumn without contentType
 *
 * When a LazyList has different types of items (header, content, footer),
 * using contentType helps Compose reuse compositions efficiently.
 */
@Composable
fun LazyListContentType_Violation(users: List<User>) {
    // WARNING: LazyColumn has heterogeneous items without contentType
    LazyColumn {
        item { Text("Header") }              // Type 1: Header
        items(users) { user ->               // Type 2: User items
            Text(user.name)
        }
        item { Text("Footer") }              // Type 3: Footer
    }
}

/**
 * VIOLATION: LazyColumn with stickyHeader but no contentType
 */
@Composable
fun LazyListStickyHeader_Violation(
    groupedItems: Map<String, List<Item>>
) {
    // WARNING: heterogeneous items without contentType
    LazyColumn {
        groupedItems.forEach { (category, items) ->
            stickyHeader { Text(category) }   // Sticky header type
            items(items) { item ->            // Item type
                Text(item.title)
            }
        }
    }
}

/**
 * CORRECT: Heterogeneous LazyColumn with contentType
 */
@Composable
fun LazyListWithContentType_Correct(users: List<User>) {
    LazyColumn {
        item(contentType = "header") { Text("Header") }
        items(users, key = { it.id }, contentType = { "user" }) { user ->
            Text(user.name)
        }
        item(contentType = "footer") { Text("Footer") }
    }
}

// =============================================================================
// 3. DerivedStateOfCandidate - computed values should use remember with keys
// =============================================================================

/**
 * VIOLATION: Computed value without remember
 *
 * Expensive computations like filter, map, sorted run on every recomposition.
 * Using remember with keys caches the result until dependencies change.
 */
@Composable
fun DerivedStateOfCandidate_Filter_Violation(
    items: List<Item>,
    searchQuery: String
) {
    // INFO: Consider using remember with keys for computed value 'filteredItems'
    val filteredItems = items.filter { it.title.contains(searchQuery) }

    LazyColumn {
        items(filteredItems, key = { it.id }) { item ->
            Text(item.title)
        }
    }
}

/**
 * VIOLATION: Multiple expensive operations
 */
@Composable
fun DerivedStateOfCandidate_Multiple_Violation(items: List<Item>) {
    // INFO: Consider using remember with keys - 'sortedItems'
    val sortedItems = items.sortedBy { it.title }

    // INFO: Consider using remember with keys - 'groupedItems'
    val groupedItems = items.groupBy { it.category }

    // INFO: Consider using remember with keys - 'itemNames'
    val itemNames = items.map { it.title }.joinToString(", ")

    Text(itemNames)
}

/**
 * CORRECT: Using remember with keys for computed values
 */
@Composable
fun DerivedStateOf_Correct(
    items: List<Item>,
    searchQuery: String
) {
    val filteredItems = remember(items, searchQuery) {
        items.filter { it.title.contains(searchQuery) }
    }

    LazyColumn {
        items(filteredItems, key = { it.id }) { item ->
            Text(item.title)
        }
    }
}

// =============================================================================
// 4. FrequentRecomposition - suggest collectAsStateWithLifecycle for flows
// =============================================================================

/**
 * VIOLATION: Flow collection without lifecycle awareness
 *
 * collectAsState() continues collecting even when the app is in the background.
 */
@Composable
fun FlowCollection_Violation(viewModel: ViewModel) {
    // WARNING: Consider using collectAsStateWithLifecycle for lifecycle awareness
    val users by viewModel.users.collectAsState(initial = emptyList())

    LazyColumn {
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
    }
}

/**
 * CORRECT: Using lifecycle-aware collector
 */
@Composable
fun FlowCollection_Correct(viewModel: ViewModel) {
    // Uses collectAsStateWithLifecycle - stops collecting when in background
    val users by viewModel.users.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn {
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
    }
}

// =============================================================================
// COMBINED VIOLATIONS - Multiple rules in one composable
// =============================================================================

/**
 * VIOLATION: Multiple experimental rule violations
 *
 * This composable demonstrates multiple issues that experimental rules catch.
 */
@Composable
fun MultipleExperimentalViolations(
    users: List<User>,
    searchQuery: String,
    viewModel: ViewModel
) {
    // DerivedStateOfCandidate: Computed without remember
    val filteredUsers = users.filter { it.name.contains(searchQuery) }

    Column {
        Text("Users")

        // LazyListContentType: Heterogeneous without contentType
        LazyColumn {
            item { Text("Header") }

            // LazyListMissingKey: No key parameter
            items(filteredUsers) { user ->
                Button(onClick = { viewModel.onUserClick(user) }) {
                    Text(user.name)
                }
            }

            item { Text("Footer") }
        }
    }
}

/**
 * CORRECT: All issues fixed
 */
@Composable
fun MultipleExperimentalViolations_Fixed(
    users: List<User>,
    searchQuery: String,
    viewModel: ViewModel
) {
    // Using remember with keys
    val filteredUsers = remember(users, searchQuery) {
        users.filter { it.name.contains(searchQuery) }
    }

    Column {
        Text("Users")

        LazyColumn {
            // With contentType
            item(contentType = "header") { Text("Header") }

            // With key and contentType
            items(
                filteredUsers,
                key = { it.id },
                contentType = { "user" }
            ) { user ->
                Button(onClick = { viewModel.onUserClick(user) }) {
                    Text(user.name)
                }
            }

            item(contentType = "footer") { Text("Footer") }
        }
    }
}
