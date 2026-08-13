# Inventory Tracker — User Guide

A practical guide to using the **Inventory Tracker** Android app.

## What This App Does

Inventory Tracker helps you keep a digital record of the things you have in stock — products, their quantities, categories, and a full history of every change. It is **100% offline**: everything is stored on your phone. No account, no internet, no data collection.

## Contents

- [Installing the App](#installing-the-app)
- [First Launch](#first-launch)
- [The Home Screen (Dashboard)](#the-home-screen-dashboard)
- [Scanning Barcodes](#scanning-barcodes)
- [Adding a Product](#adding-a-product)
- [The Products List](#the-products-list)
- [Product Details & Adjusting Stock](#product-details--adjusting-stock)
- [Managing Categories](#managing-categories)
- [Viewing History](#viewing-history)
- [Tips & Troubleshooting](#tips--troubleshooting)

---

## Installing the App

1. If you received a **Play Store link**, tap it and press **Install**.
2. If you received an **APK file** (sideload):
   - Copy the `.apk` to your phone (or download it in a browser).
   - Tap the file to install.
   - If prompted, allow "Install unknown apps" for the app you're installing from (browser, Files, etc.) — you'll see the prompt under **Settings → Security**.
   - Confirm any Play Protect warning; the app is safe and works fully offline.

> **Requirements:** Android 8.0 (API 26) or newer.

---

## First Launch

When you open the app you'll see the **Home** screen with three tabs at the bottom:

| Tab | Purpose |
|---|---|
| **Home** | Your dashboard: category overview, low-stock alerts, recent activity |
| **Scanner** | Camera barcode scanner + manual barcode entry |
| **Products** | Search and browse all your products |

The app starts with six ready-made categories: **Electronics, Groceries, Clothing, Office Supplies, Other**, and **Uncategorized**. You can edit, delete, or add your own at any time.

---

## The Home Screen (Dashboard)

- **Category cards** (top): tap one to see all products in that category.
- **Low stock:** products whose quantity is at or below their low-stock threshold. Tap a product to open it.
- **Recent activity:** the latest inventory changes. Tap **View all** to open the full history.

---

## Scanning Barcodes

The fastest way to find or add a product.

1. Tap the **Scanner** tab.
2. The first time, the app asks for **Camera** permission — tap **Allow**. (You can still use the app without the camera; see [Manual entry](#manual-entry) below.)
3. Point the camera at a barcode and keep it inside the corner brackets. The app beeps and vibrates on a successful read.
4. The app will then:
   - **Open the product** if it's already in your inventory, or
   - **Open the "Add product" form** with the barcode already filled in, if it's new.

Supported formats: **EAN-13, UPC-A, QR code, Code 128, Code 39**.

### Torch

In the scanner, tap the **flashlight icon** in the top-right to turn the camera light on for dim rooms.

### Manual entry

If you denied the camera permission, your camera is broken, or the code won't scan:

1. Tap the **Scanner** tab.
2. Type the barcode number in the field.
3. Pick the **format** (usually EAN-13 for store products, QR for codes/links).
4. Tap **Look up**.

---

## Adding a Product

There are two ways to add a product:

- **Scan a new barcode** — the app jumps straight to the add form with the barcode pre-filled.
- From the **Products** tab → tap the **＋ Add product** button (bottom-right).

Fill in the form:

| Field | Description |
|---|---|
| **Barcode** | The scanned code, or type one. Optional if you're not using barcodes. |
| **Format** | Barcode type (EAN-13, UPC-A, QR, etc.) — auto-set when scanned. |
| **Name** | The product name. *Required.* |
| **Description** | Optional notes. |
| **Category** | Pick one from the list, or leave **Uncategorized**. |
| **Initial quantity** | Starting stock (only when adding). |
| **Low stock threshold** | The quantity that triggers a low-stock warning. E.g. set 5 and you'll be alerted when stock reaches 5 or below. |
| **Unit** | How you count it — pcs, boxes, kg, L, etc. |

Tap **Save**. You'll land on the new product's detail page.

> **Tip:** quantities are whole numbers (no decimals).

---

## The Products List

The **Products** tab shows everything you've added:

- **Search** — type to filter by name.
- **Category filter** — chip row; tap a category to show only its products.
- **Sort** — tap the sort label (Name / Quantity / Recent) to reorder.
- Tap any product row to open its detail page.

---

## Product Details & Adjusting Stock

This is where you manage a product. Tap a product from Home, Products, Scanner, or History.

**Information card:** name, barcode, format, category, description, low-stock threshold, created date, current quantity (highlighted **red** if low stock).

**Adjust quantity:**

- **Quick chips:** `+1`, `+5`, `+10` to add stock; `-1`, `-5`, `-10` to remove it.
- **Set exact quantity:** type a number and tap **Set** to make stock exactly that value.
- **Reason (optional):** type a note (e.g. "received order", "damaged") before adjusting — it's recorded with the change.

**Other actions** (⋮ menu in the top-right):

- **Edit** — change any product details.
- **Delete** — remove the product (you'll be asked to confirm).

> **Rules you might hit:**
> - Stock can never go **below zero** — a decrease that would do so is rejected.
> - Quantity is capped at **9,999,999**.
> - An adjustment of **0** does nothing.

Every successful change is saved to the product's history immediately.

---

## Managing Categories

Categories keep products organized. To open the manager, go to the **Products** tab and tap the **category (folder) icon** in the top-right corner.

- **Add:** tap the **＋ New category** floating button.
- **Name** — required.
- **Description** — optional.
- **Color** — pick a color dot; it's used across the app (cards, product rows).
- **Edit:** tap the ✏️ icon next to a category.
- **Delete:** tap the 🗑️ icon. Deleting a category does **not** delete its products — they just become **Uncategorized**.

---

## Viewing History

History is a complete, chronological record of every inventory change.

**From the Home screen:** tap **View all** next to Recent activity.

**Filters:**

- **Time range:** All time, **Today**, **7 days**, or **30 days** (chip row at the top).
- **Product:** pick a specific product from the dropdown to see only its changes.

Each entry shows the **product**, **date/time**, the change (`+5 → 42`, `-2 → 40`, `= 50` for "set"), and any **reason** you entered. Tap an entry to open that product.

---

## Tips & Troubleshooting

| Problem | Solution |
|---|---|
| Barcode won't scan | Hold the phone steadier, fill more of the brackets, move closer/farther. Try the flashlight in dim light. Use **manual entry** as a backup. |
| Camera permission denied | Everything still works — use manual barcode entry on the Scanner tab. To re-enable later: **Settings → Apps → Inventory Tracker → Permissions → Camera**. |
| Stock shows red | The product is **at or below** its low-stock threshold. Increase stock or edit the threshold. |
| Can't set a negative number | Stock can't go below 0 by design. |
| Deleted a product by accident | There's no undo yet — delete is permanent. Double-check the confirmation dialog. |
| Where's my data? | It's stored **only on this phone** (local SQLite database). It also **backs up to your Google account** when auto-backup is enabled (the app declares backup rules). |
| Changing devices | Restore the backup on the new phone (sign in with the same Google account). Manual backups (export) aren't available in this version. |
| App is slow after many products | It should stay fast — the list is lazy-loaded. If needed, use search and category filters. |

**Good practices:**

- Set **low-stock thresholds** on your important products so the dashboard keeps you informed.
- Use the **reason** field for adjustments you might want to audit later ("damaged", "received", "count correction").
- Do a **stock count** periodically and use **Set exact quantity** to fix discrepancies — each correction is logged.
