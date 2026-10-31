#!/usr/bin/env python3
"""Tiny uiautomator helper for the emulator check: tap a node by its visible text."""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


def adb(*args):
    return subprocess.run(["adb", *args], capture_output=True, text=True, check=False).stdout


def dump_xml():
    """Dump the UI tree, retrying because uiautomator fails on constantly animating screens."""
    for attempt in range(6):
        adb("shell", "rm", "-f", "/sdcard/ui.xml")
        message = adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
        xml = adb("exec-out", "cat", "/sdcard/ui.xml")
        if xml.strip().startswith("<?xml") and "<node" in xml:
            return xml
        print(f"dump attempt {attempt + 1} failed: {message.strip()[:120]}")
        time.sleep(1.5)
    return ""


def center(node):
    match = BOUNDS.match(node.get("bounds", ""))
    if not match:
        return None
    left, top, right, bottom = (int(v) for v in match.groups())
    return (left + right) // 2, (top + bottom) // 2


def find(xml, needle):
    try:
        root = ET.fromstring(xml)
    except ET.ParseError:
        return None
    needle = needle.lower()
    fallback = None
    for node in root.iter("node"):
        label = ((node.get("text") or "") + " " + (node.get("content-desc") or "")).lower()
        if needle not in label:
            continue
        point = center(node)
        if point is None:
            continue
        if node.get("clickable") == "true":
            return point
        if fallback is None:
            fallback = point
    return fallback


def screen_size():
    out = adb("shell", "wm", "size")
    match = re.search(r"(\d+)x(\d+)", out)
    return (int(match.group(1)), int(match.group(2))) if match else (1080, 2400)


def tap_point(point):
    adb("shell", "input", "tap", str(point[0]), str(point[1]))


def settled(needle, first):
    """Re-dump after a short pause and return the position once two dumps agree."""
    for _ in range(4):
        time.sleep(0.8)
        second = find(dump_xml(), needle)
        if second is not None and abs(second[1] - first[1]) < 6:
            return second
        first = second if second is not None else first
    return first


def swipe(width, height, up):
    x = str(width // 2)
    start, end = (0.75, 0.30) if up else (0.30, 0.75)
    adb("shell", "input", "swipe", x, str(int(height * start)), x, str(int(height * end)), "300")


def cmd_tap(needle):
    point = find(dump_xml(), needle)
    if point is None:
        print(f"NOT FOUND: {needle}")
        return 1
    point = settled(needle, point)
    tap_point(point)
    print(f"tapped: {needle} at {point}")
    return 0


def cmd_scroll_tap(needle):
    width, height = screen_size()
    for direction_up, tries in ((True, 7), (False, 14)):
        for _ in range(tries):
            point = find(dump_xml(), needle)
            if point is not None:
                point = settled(needle, point)
                tap_point(point)
                print(f"tapped: {needle} at {point}")
                return 0
            swipe(width, height, direction_up)
            time.sleep(0.6)
    print(f"NOT FOUND after scrolling: {needle}")
    return 1


def cmd_has(needle):
    found = find(dump_xml(), needle) is not None
    print("yes" if found else "no")
    return 0 if found else 1


def cmd_texts():
    try:
        root = ET.fromstring(dump_xml())
    except ET.ParseError:
        print("(no hierarchy)")
        return 0
    for node in root.iter("node"):
        text = (node.get("text") or node.get("content-desc") or "").strip()
        if text:
            print(text.replace("\n", " "))
    return 0


def main():
    if len(sys.argv) < 2:
        print("usage: emu_ui.py tap|scroll-tap|has <text> | texts")
        return 2
    action = sys.argv[1]
    arg = sys.argv[2] if len(sys.argv) > 2 else ""
    handlers = {"tap": cmd_tap, "scroll-tap": cmd_scroll_tap, "has": cmd_has}
    if action == "texts":
        return cmd_texts()
    if action in handlers:
        return handlers[action](arg)
    print(f"unknown action: {action}")
    return 2


if __name__ == "__main__":
    sys.exit(main())
