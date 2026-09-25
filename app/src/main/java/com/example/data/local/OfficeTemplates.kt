package com.example.data.local

import com.example.data.model.FileType
import com.example.data.model.OfficeFile

object OfficeTemplates {

    const val SAMPLE_DOCUMENT_CONTENT = """
{
  "title": "Quarterly Business Proposal",
  "blocks": [
    {"type": "heading1", "text": "NOVA Strategic Growth Plan 2026", "bold": true, "italic": false, "align": "center"},
    {"type": "paragraph", "text": "Executive Summary: In this quarter, our primary focus is modern mobile productivity and seamless touch-first document workflows across distributed teams.", "bold": false, "italic": false, "align": "left"},
    {"type": "heading2", "text": "1. Key Performance Indicators", "bold": true, "italic": false, "align": "left"},
    {"type": "bullet", "text": "Deliver 99.9% offline capability across Document, Sheet, and Slide suites.", "bold": false, "italic": false, "align": "left"},
    {"type": "bullet", "text": "Provide sub-100ms formula calculation speed on complex sheets.", "bold": false, "italic": false, "align": "left"},
    {"type": "bullet", "text": "One-touch export to high-fidelity PDF and direct print spooling.", "bold": false, "italic": false, "align": "left"},
    {"type": "heading2", "text": "2. Financial Allocation", "bold": true, "italic": false, "align": "left"},
    {"type": "paragraph", "text": "Capital deployment is balanced across engineering velocity, touch ergonomics, and security sandboxing. All customer files remain strictly localized on-device.", "bold": false, "italic": false, "align": "left"}
  ],
  "margins": "Normal",
  "orientation": "Portrait"
}
"""

    const val SAMPLE_SPREADSHEET_CONTENT = """
{
  "activeSheetIndex": 0,
  "sheets": [
    {
      "name": "Budget 2026",
      "rowCount": 20,
      "colCount": 8,
      "cells": {
        "A1": {"value": "Category", "format": "General", "bold": true, "bg": "#E2E8F0"},
        "B1": {"value": "Q1 Target", "format": "General", "bold": true, "bg": "#E2E8F0"},
        "C1": {"value": "Q2 Target", "format": "General", "bold": true, "bg": "#E2E8F0"},
        "D1": {"value": "Total Projected", "format": "General", "bold": true, "bg": "#E2E8F0"},
        "A2": {"value": "Marketing", "format": "General", "bold": false, "bg": ""},
        "B2": {"value": "12000", "format": "Currency", "bold": false, "bg": ""},
        "C2": {"value": "14500", "format": "Currency", "bold": false, "bg": ""},
        "D2": {"value": "=SUM(B2:C2)", "format": "Currency", "bold": true, "bg": "#F1F5F9"},
        "A3": {"value": "Engineering", "format": "General", "bold": false, "bg": ""},
        "B3": {"value": "35000", "format": "Currency", "bold": false, "bg": ""},
        "C3": {"value": "42000", "format": "Currency", "bold": false, "bg": ""},
        "D3": {"value": "=SUM(B3:C3)", "format": "Currency", "bold": true, "bg": "#F1F5F9"},
        "A4": {"value": "Operations", "format": "General", "bold": false, "bg": ""},
        "B4": {"value": "8500", "format": "Currency", "bold": false, "bg": ""},
        "C4": {"value": "9200", "format": "Currency", "bold": false, "bg": ""},
        "D4": {"value": "=SUM(B4:C4)", "format": "Currency", "bold": true, "bg": "#F1F5F9"},
        "A5": {"value": "Total Budget", "format": "General", "bold": true, "bg": "#DBEAFE"},
        "B5": {"value": "=SUM(B2:B4)", "format": "Currency", "bold": true, "bg": "#DBEAFE"},
        "C5": {"value": "=SUM(C2:C4)", "format": "Currency", "bold": true, "bg": "#DBEAFE"},
        "D5": {"value": "=SUM(D2:D4)", "format": "Currency", "bold": true, "bg": "#BFDBFE"}
      }
    },
    {
      "name": "Q1 Performance",
      "rowCount": 15,
      "colCount": 6,
      "cells": {
        "A1": {"value": "Metric", "bold": true, "format": "General", "bg": "#E2E8F0"},
        "B1": {"value": "Score", "bold": true, "format": "General", "bg": "#E2E8F0"},
        "A2": {"value": "Customer Retention", "bold": false, "format": "General", "bg": ""},
        "B2": {"value": "94.5", "bold": false, "format": "Percentage", "bg": ""},
        "A3": {"value": "System Uptime", "bold": false, "format": "General", "bg": ""},
        "B3": {"value": "99.9", "bold": false, "format": "Percentage", "bg": ""},
        "A4": {"value": "Average Rating", "bold": false, "format": "General", "bg": ""},
        "B4": {"value": "=AVERAGE(B2:B3)", "bold": true, "format": "Decimal", "bg": "#FEF3C7"}
      }
    }
  ]
}
"""

    const val SAMPLE_PRESENTATION_CONTENT = """
{
  "theme": "Corporate Blue",
  "slides": [
    {
      "title": "NOVA Office Suite",
      "subtitle": "Everything You Need to Create on Mobile",
      "layout": "TITLE_SLIDE",
      "bulletPoints": [],
      "notes": "Welcome everyone to the launch presentation. Highlight touch-first ergonomics."
    },
    {
      "title": "Comprehensive Mobile Capabilities",
      "subtitle": "All core desktop workflows reimagined for Android",
      "layout": "TITLE_AND_CONTENT",
      "bulletPoints": [
        "Rich text document formatting with quick keyboard bar",
        "Excel-grade spreadsheet formula evaluation engine",
        "Interactive canvas presentations with presenter notes & timer",
        "Desktop publisher page designer for posters & flyers",
        "Integrated PDF toolkit and local file manager"
      ],
      "notes": "Walk through the universal app switcher and seamless auto-save."
    },
    {
      "title": "Security & Architecture",
      "subtitle": "100% Offline-First and Private",
      "layout": "TWO_COLUMNS",
      "bulletPoints": [
        "Local Room SQL database",
        "Zero tracking or telemetry",
        "PIN protection & app lock"
      ],
      "notes": "Emphasize enterprise safety and zero cloud requirement."
    }
  ]
}
"""

    const val SAMPLE_PUBLISHER_CONTENT = """
{
  "canvasWidth": 360,
  "canvasHeight": 540,
  "backgroundColor": "#0F172A",
  "elements": [
    {"id": 1, "type": "badge", "text": "SPECIAL EVENT", "x": 100, "y": 30, "color": "#F59E0B", "fontSize": 14},
    {"id": 2, "type": "heading", "text": "TECH EXPO 2026", "x": 40, "y": 90, "color": "#FFFFFF", "fontSize": 28},
    {"id": 3, "type": "subheading", "text": "Next-Gen Mobile Innovation", "x": 55, "y": 140, "color": "#38BDF8", "fontSize": 16},
    {"id": 4, "type": "divider", "text": "", "x": 40, "y": 180, "color": "#475569", "fontSize": 2},
    {"id": 5, "type": "paragraph", "text": "Join industry leaders for hands-on demos, keynote sessions, and networking.", "x": 35, "y": 210, "color": "#CBD5E1", "fontSize": 13},
    {"id": 6, "type": "box", "text": "DATE: NOV 14-16 • SAN FRANCISCO", "x": 35, "y": 300, "color": "#1E293B", "fontSize": 12},
    {"id": 7, "type": "button", "text": "REGISTER NOW • ADMISSION FREE", "x": 45, "y": 420, "color": "#2563EB", "fontSize": 14}
  ]
}
"""

    const val SAMPLE_FORM_CONTENT = """
{
  "title": "Customer Feedback Survey",
  "description": "Please help us improve NOVA Office by sharing your experience.",
  "fields": [
    {"id": 1, "label": "Full Name", "type": "TEXT", "required": true, "value": ""},
    {"id": 2, "label": "Email Address", "type": "TEXT", "required": true, "value": ""},
    {"id": 3, "label": "Primary Use Case", "type": "DROPDOWN", "options": ["Business & Finance", "Academic / Student", "Personal Planning", "Design & Publishing"], "value": "Business & Finance"},
    {"id": 4, "label": "Overall Satisfaction (1-5)", "type": "NUMBER", "required": true, "value": "5"},
    {"id": 5, "label": "Would you recommend NOVA Office?", "type": "RADIO", "options": ["Definitely Yes", "Probably", "Undecided"], "value": "Definitely Yes"},
    {"id": 6, "label": "Features Used Regularly", "type": "CHECKBOX", "options": ["Documents", "Spreadsheets", "Presentations", "PDF Tools"], "value": "Documents, Spreadsheets"},
    {"id": 7, "label": "Additional Comments", "type": "TEXT", "required": false, "value": ""}
  ],
  "responsesCount": 18
}
"""

    const val SAMPLE_PDF_CONTENT = """
{
  "title": "NOVA Office User Manual",
  "pageCount": 4,
  "pages": [
    {"pageNumber": 1, "title": "Welcome to NOVA Office", "content": "NOVA Office is an all-in-one mobile productivity suite crafted for Android. It unites rich document editing, multi-sheet spreadsheets with full formula evaluation, slide presentations, desktop publisher layout tools, forms, and a secure file manager."},
    {"pageNumber": 2, "title": "Touch Ergonomics & Keyboard Toolbar", "content": "When editing on a smartphone, formatting controls are positioned directly above the soft keyboard or in bottom sheets, eliminating awkward reaching to screen tops. Undo and Redo are instantly accessible with single taps."},
    {"pageNumber": 3, "title": "Formula Engine & Spreadsheets", "content": "Support for over 20 essential formulas including SUM, AVERAGE, MIN, MAX, COUNT, IF, AND, OR, ROUND, SQRT, and CONCAT. Visual charts update immediately when values change."},
    {"pageNumber": 4, "title": "Exporting & Android Native Printing", "content": "Easily print or export your documents to PDF, share via Android's native share sheet, or protect sensitive documents with the built-in app PIN lock."}
  ]
}
"""

    fun getInitialSeedFiles(): List<OfficeFile> {
        val now = System.currentTimeMillis()
        return listOf(
            OfficeFile(
                id = 1L,
                title = "Quarterly Business Proposal",
                fileType = FileType.DOCUMENT.name,
                contentJson = SAMPLE_DOCUMENT_CONTENT,
                lastModified = now - 1000 * 60 * 15,
                sizeBytes = 24500L,
                isFavorite = true,
                category = "Business"
            ),
            OfficeFile(
                id = 2L,
                title = "Annual Financial Budget 2026",
                fileType = FileType.SPREADSHEET.name,
                contentJson = SAMPLE_SPREADSHEET_CONTENT,
                lastModified = now - 1000 * 60 * 45,
                sizeBytes = 38200L,
                isFavorite = true,
                category = "Finance"
            ),
            OfficeFile(
                id = 3L,
                title = "Product Launch Keynote",
                fileType = FileType.PRESENTATION.name,
                contentJson = SAMPLE_PRESENTATION_CONTENT,
                lastModified = now - 1000 * 60 * 120,
                sizeBytes = 142000L,
                isFavorite = false,
                category = "Marketing"
            ),
            OfficeFile(
                id = 4L,
                title = "Tech Expo 2026 Poster",
                fileType = FileType.PUBLISHER.name,
                contentJson = SAMPLE_PUBLISHER_CONTENT,
                lastModified = now - 1000 * 60 * 300,
                sizeBytes = 85000L,
                isFavorite = false,
                category = "Creative"
            ),
            OfficeFile(
                id = 5L,
                title = "Customer Feedback Survey",
                fileType = FileType.FORM.name,
                contentJson = SAMPLE_FORM_CONTENT,
                lastModified = now - 1000 * 60 * 600,
                sizeBytes = 16400L,
                isFavorite = true,
                category = "Feedback"
            ),
            OfficeFile(
                id = 6L,
                title = "NOVA Office User Manual",
                fileType = FileType.PDF.name,
                contentJson = SAMPLE_PDF_CONTENT,
                lastModified = now - 1000 * 60 * 1440,
                sizeBytes = 210000L,
                isFavorite = false,
                category = "Documentation"
            )
        )
    }
}
