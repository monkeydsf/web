function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderInline(text) {
  return escapeHtml(text)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
}

function closeList(state) {
  if (!state.listType) return ''
  const tag = state.listType
  state.listType = ''
  return `</${tag}>`
}

function renderTable(lines, startIndex) {
  const header = lines[startIndex].trim()
  const separator = lines[startIndex + 1]?.trim()
  if (!header.includes('|') || !/^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$/.test(separator || '')) {
    return null
  }

  const parseCells = (line) => line
    .trim()
    .replace(/^\|/, '')
    .replace(/\|$/, '')
    .split('|')
    .map((cell) => cell.trim())

  const headers = parseCells(header)
  const rows = []
  let index = startIndex + 2

  while (index < lines.length && lines[index].trim().includes('|')) {
    rows.push(parseCells(lines[index]))
    index += 1
  }

  const thead = `<thead><tr>${headers.map((cell) => `<th>${renderInline(cell)}</th>`).join('')}</tr></thead>`
  const tbody = rows.length
    ? `<tbody>${rows.map((row) => `<tr>${row.map((cell) => `<td>${renderInline(cell)}</td>`).join('')}</tr>`).join('')}</tbody>`
    : ''

  return {
    html: `<div class="markdown-table-wrap"><table>${thead}${tbody}</table></div>`,
    nextIndex: index
  }
}

export function renderMarkdown(markdown, fallback = '') {
  const source = String(markdown || fallback || '').replace(/\r\n/g, '\n')
  const lines = source.split('\n')
  const state = { listType: '' }
  let html = ''
  let inCode = false
  let code = []

  for (let index = 0; index < lines.length; index += 1) {
    const rawLine = lines[index]
    const trimmed = rawLine.trim()

    if (trimmed.startsWith('```')) {
      if (inCode) {
        html += `<pre><code>${escapeHtml(code.join('\n'))}</code></pre>`
        code = []
        inCode = false
      } else {
        html += closeList(state)
        inCode = true
      }
      continue
    }

    if (inCode) {
      code.push(rawLine)
      continue
    }

    if (!trimmed) {
      html += closeList(state)
      continue
    }

    const table = renderTable(lines, index)
    if (table) {
      html += closeList(state)
      html += table.html
      index = table.nextIndex - 1
      continue
    }

    const heading = trimmed.match(/^(#{1,6})\s+(.+)$/)
    if (heading) {
      html += closeList(state)
      const level = Math.min(heading[1].length, 4)
      html += `<h${level}>${renderInline(heading[2])}</h${level}>`
      continue
    }

    if (/^[-*_]{3,}$/.test(trimmed)) {
      html += closeList(state)
      html += '<hr>'
      continue
    }

    const unordered = trimmed.match(/^[-*]\s+(.+)$/)
    const ordered = trimmed.match(/^\d+[.)]\s+(.+)$/)
    const listType = ordered ? 'ol' : unordered ? 'ul' : ''
    if (listType) {
      if (state.listType !== listType) {
        html += closeList(state)
        html += `<${listType}>`
        state.listType = listType
      }
      html += `<li>${renderInline((ordered || unordered)[1])}</li>`
      continue
    }

    const quote = trimmed.match(/^>\s?(.+)$/)
    if (quote) {
      html += closeList(state)
      html += `<blockquote>${renderInline(quote[1])}</blockquote>`
      continue
    }

    html += closeList(state)
    html += `<p>${renderInline(trimmed)}</p>`
  }

  html += closeList(state)
  if (inCode) {
    html += `<pre><code>${escapeHtml(code.join('\n'))}</code></pre>`
  }

  return html
}
