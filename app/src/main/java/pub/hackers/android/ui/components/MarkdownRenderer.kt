package pub.hackers.android.ui.components

/**
 * Simple markdown to HTML converter for compose preview.
 * Handles common markdown patterns used in HackersPub posts.
 */
fun markdownToHtml(markdown: String): String {
    var html = markdown

    // Escape HTML entities first
    html = html.replace("&", "&amp;")
    html = html.replace("<", "&lt;")
    html = html.replace(">", "&gt;")

    // Code blocks (``` ... ```) - must be before inline code
    html = html.replace(Regex("```([\\s\\S]*?)```")) { match ->
        "<pre><code>${match.groupValues[1].trim()}</code></pre>"
    }

    // Inline code (`...`)
    html = html.replace(Regex("`([^`]+)`")) { match ->
        "<code>${match.groupValues[1]}</code>"
    }

    // Headings (must be at start of line)
    html = html.replace(Regex("(?m)^######\\s+(.+)")) { "<h6>${it.groupValues[1]}</h6>" }
    html = html.replace(Regex("(?m)^#####\\s+(.+)")) { "<h5>${it.groupValues[1]}</h5>" }
    html = html.replace(Regex("(?m)^####\\s+(.+)")) { "<h4>${it.groupValues[1]}</h4>" }
    html = html.replace(Regex("(?m)^###\\s+(.+)")) { "<h3>${it.groupValues[1]}</h3>" }
    html = html.replace(Regex("(?m)^##\\s+(.+)")) { "<h2>${it.groupValues[1]}</h2>" }
    html = html.replace(Regex("(?m)^#\\s+(.+)")) { "<h1>${it.groupValues[1]}</h1>" }

    // Bold and italic (order matters)
    html = html.replace(Regex("\\*\\*\\*(.+?)\\*\\*\\*")) { "<strong><em>${it.groupValues[1]}</em></strong>" }
    html = html.replace(Regex("\\*\\*(.+?)\\*\\*")) { "<strong>${it.groupValues[1]}</strong>" }
    html = html.replace(Regex("\\*(.+?)\\*")) { "<em>${it.groupValues[1]}</em>" }

    // Strikethrough
    html = html.replace(Regex("~~(.+?)~~")) { "<del>${it.groupValues[1]}</del>" }

    // Links [text](url)
    html = html.replace(Regex("\\[([^]]+)]\\(([^)]+)\\)")) { match ->
        "<a href=\"${match.groupValues[2]}\">${match.groupValues[1]}</a>"
    }

    // Blockquotes
    html = html.replace(Regex("(?m)^>\\s*(.+)")) { "<blockquote>${it.groupValues[1]}</blockquote>" }

    // Unordered lists
    html = html.replace(Regex("(?m)^[*-]\\s+(.+)")) { "<li>${it.groupValues[1]}</li>" }

    // Horizontal rules
    html = html.replace(Regex("(?m)^---+\\s*$")) { "<hr>" }

    // Paragraphs (double newlines)
    html = html.replace(Regex("\n\n+")) { "</p><p>" }
    html = "<p>$html</p>"

    // Single newlines to <br>
    html = html.replace("\n", "<br>")

    // Clean up empty paragraphs
    html = html.replace("<p></p>", "")
    html = html.replace("<p><br></p>", "")

    return html
}
