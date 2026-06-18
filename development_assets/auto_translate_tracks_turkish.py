# Please do not run this unless you are Slimeist (techno-sam), the author of the script (or he has explained it to you)

import os
import json


CAPITALIZE_FIRST_ONLY = False
NON_CAPITALIZED_WORDS = {
    "ve"
}

track_materials: dict[str, str] = {
    "acacia": "akasya",
    "birch": "huş",
    "dark_oak": "koyu meşe",
    "jungle": "orman ağacı",
    "oak": "meşe",
    "spruce": "ladin",
    "mangrove": "mangrov",
    "warped": "çarpık",
    "crimson": "kızıl",
    "blackstone": "kara taş",
    "ender": "ender",
    "tieless": "bağsız",
    "phantom": "kâbustan",
    "cherry": "kiraz ağacı",
    "bamboo": "bambu",
    "stripped_bamboo": "soyulmuş bambu",
    "byg_aspen": "kavak",
    "natures_spirit_aspen": "kavak",
    "tfc_aspen": "kavak",
    "byg_baobab": "baobap ağacı"
}

track_variants: dict[str, str] = {
    "wide": "geniş",
    "narrow": "dar",
}


def capitalize(s: str) -> str:
    s = s.lower()
    if len(s) > 0 and s.lower() not in NON_CAPITALIZED_WORDS:
        return s[0].upper() + s[1:]
    else:
        return s


def join_with_title_case(*parts: str | tuple[str, bool]) -> str:
    new_parts = []
    for p in parts:
        if type(p) == str:
            new_parts.append(p)
        elif type(p) == tuple:
            if p[1]:
                new_parts.append(p[0])
        else:
            raise ValueError(f"Invalid type {type(p)}")
    new_parts = [p for p in new_parts if p != ""]
    new_parts = " ".join(new_parts).split(" ")
    if CAPITALIZE_FIRST_ONLY:
        new_parts[0] = capitalize(new_parts[0])
    else:
        new_parts = [capitalize(p) for p in new_parts if p != ""]
    return " ".join(new_parts)


def mk_incomplete(variant: str | None) -> callable:
    """
    :param variant: None, wide, narrow
    :return:
    """
    def f(material: str) -> str:
        return join_with_title_case(
            "Tamamlanmamış",
            track_variants.get(variant, ""),
            track_materials.get(material),
            "ray"
        )
    return f


def mk_track(variant: str | None) -> callable:
    """
    :param variant: None, wide, narrow
    :return:
    """
    def f(material: str) -> str:
        return join_with_title_case(
            track_variants.get(variant, ""),
            track_materials.get(material),
            "tren rayı"
        )
    return f


examples = {
    # Incomplete
    "item.railways.track_incomplete_acacia": "Incomplete Acacia Track",
    "item.railways.track_incomplete_acacia_narrow": "Incomplete Narrow Acacia Track",
    "item.railways.track_incomplete_acacia_wide": "Incomplete Wide Acacia Track",

    # Blocks
    "block.railways.track_acacia": "Acacia Train Track",
    "block.railways.track_acacia_narrow": "Narrow Acacia Train Track",
    "block.railways.track_acacia_wide": "Wide Acacia Train Track",
}


translations: dict[str, callable] = {
    "item.railways.track_incomplete_{material}": mk_incomplete(None),
    "item.railways.track_incomplete_{material}_narrow": mk_incomplete("narrow"),
    "item.railways.track_incomplete_{material}_wide": mk_incomplete("wide"),

    "block.railways.track_{material}": mk_track(None),
    "block.railways.track_{material}_narrow": mk_track("narrow"),
    "block.railways.track_{material}_wide": mk_track("wide"),
}


with open("../common/src/generated/resources/assets/railways/lang/en_us.json", "r") as f:
    source_strings = json.load(f)
source_strings: dict[str, str]

lang = "tr_tr"

with open(f"../common/src/main/resources/assets/railways/lang/{lang}.json", "r") as f:
    existing_translated_strings = json.load(f)
existing_translated_strings: dict[str, str]

new_translated_strings: dict[str, str] = {}


for string, formatter in translations.items():
    for mat in track_materials:
        s = string.format(material=mat)
        if s not in source_strings:
            print("OOPS", s)
            continue
        if s in existing_translated_strings:
            print("Already translated", s)
            continue
        new_translated_strings[s] = formatter(mat)

print(f"New translations for {lang}")
for k, v in new_translated_strings.items():
    print(f"  {k}: {v}")

# quit()
all_strings = existing_translated_strings.copy()
all_strings.update(new_translated_strings)
with open(f"../common/src/main/resources/assets/railways/lang/{lang}.json", "w") as f:
    json.dump(all_strings, f, indent=2, ensure_ascii=False)
