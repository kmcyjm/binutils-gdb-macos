#
# Unusual variables checked by this code:
#	NOP - four byte opcode for no-op (defaults to 0)
#	NO_SMALL_DATA - no .sbss/.sbss2/.sdata/.sdata2 sections if not
#		empty.
#	DATA_ADDR - if end-of-text-plus-one-page isn't right for data start
#	INITIAL_READONLY_SECTIONS - at start of text segment
#	OTHER_READONLY_SECTIONS - other than .text .init .rodata ...
#		(e.g., .PARISC.milli)
#	OTHER_TEXT_SECTIONS - these get put in .text when relocating
#	OTHER_READWRITE_SECTIONS - other than .data .bss .ctors .sdata ...
#		(e.g., .PARISC.global)
#	OTHER_BSS_SECTIONS - other than .bss .sbss ...
#	OTHER_SECTIONS - at the end
#	EXECUTABLE_SYMBOLS - symbols that must be defined for an
#		executable (e.g., _DYNAMIC_LINK)
#	TEXT_START_SYMBOLS - symbols that appear at the start of the
#		.text section.
#	DATA_START_SYMBOLS - symbols that appear at the start of the
#		.data section.
#	OTHER_GOT_SYMBOLS - symbols defined just before .got.
#	OTHER_GOT_SECTIONS - sections just after .got.
#	OTHER_SDATA_SECTIONS - sections just after .sdata.
#	OTHER_BSS_SYMBOLS - symbols that appear at the start of the
#		.bss section besides __bss_start.
#	DATA_PLT - .plt should be in data segment, not text segment.
#	BSS_PLT - .plt should be in bss segment
#	TEXT_DYNAMIC - .dynamic in text segment, not data segment.
#	EMBEDDED - whether this is for an embedded system.
#	SHLIB_TEXT_START_ADDR - if set, add to SIZEOF_HEADERS to set
#		start address of shared library.
#	INPUT_FILES - INPUT command of files to always include
#	WRITABLE_RODATA - if set, the .rodata section should be writable
#	INIT_START, INIT_END -  statements just before and just after
#	combination of .init sections.
#	FINI_START, FINI_END - statements just before and just after
#	combination of .fini sections.
#	STACK_ADDR - start of a .stack section.
#	OTHER_END_SYMBOLS - symbols to place right at the end of the script.
#
# When adding sections, do note that the names of some sections are used
# when specifying the start address of the next.
#

#  Many sections come in three flavours.  There is the 'real' section,
#  like ".data".  Then there are the per-procedure or per-variable
#  sections, generated by -ffunction-sections and -fdata-sections in GCC,
#  and useful for --gc-sections, which for a variable "foo" might be
#  ".data.foo".  Then there are the linkonce sections, for which the linker
#  eliminates duplicates, which are named like ".gnu.linkonce.d.foo".
#  The exact correspondences are:
#
#  Section	Linkonce section
#  .text	.gnu.linkonce.t.foo
#  .rodata	.gnu.linkonce.r.foo
#  .data	.gnu.linkonce.d.foo
#  .bss		.gnu.linkonce.b.foo
#  .sdata	.gnu.linkonce.s.foo
#  .sbss	.gnu.linkonce.sb.foo
#  .sdata2	.gnu.linkonce.s2.foo
#  .sbss2	.gnu.linkonce.sb2.foo
#  .debug_info	.gnu.linkonce.wi.foo
#  .tdata	.gnu.linkonce.td.foo
#  .tbss	.gnu.linkonce.tb.foo
#
#  Each of these can also have corresponding .rel.* and .rela.* sections.

test -z "$ENTRY" && ENTRY=start
test -z "${BIG_OUTPUT_FORMAT}" && BIG_OUTPUT_FORMAT=${OUTPUT_FORMAT}
test -z "${LITTLE_OUTPUT_FORMAT}" && LITTLE_OUTPUT_FORMAT=${OUTPUT_FORMAT}
# If we request a big endian toolchain, give a big endian linker
test "${ARC_ENDIAN}" = "big" && OUTPUT_FORMAT=${BIG_OUTPUT_FORMAT}
if [ -z "$MACHINE" ]; then OUTPUT_ARCH=${ARCH}; else OUTPUT_ARCH=${ARCH}:${MACHINE}; fi
test -z "${ELFSIZE}" && ELFSIZE=32
test -z "${ALIGNMENT}" && ALIGNMENT="${ELFSIZE} / 8"
test "$LD_FLAG" = "N" && DATA_ADDR=.
test -n "$CREATE_SHLIB$CREATE_PIE" && test -n "$SHLIB_DATA_ADDR" && COMMONPAGESIZE=""
test -z "$CREATE_SHLIB$CREATE_PIE" && test -n "$DATA_ADDR" && COMMONPAGESIZE=""
DATA_SEGMENT_ALIGN="ALIGN(${SEGMENT_SIZE}) + (. & (${MAXPAGESIZE} - 1))"
DATA_SEGMENT_END=""
if test -n "${COMMONPAGESIZE}"; then
  DATA_SEGMENT_ALIGN="ALIGN (${SEGMENT_SIZE}) - ((${MAXPAGESIZE} - .) & (${MAXPAGESIZE} - 1)); . = DATA_SEGMENT_ALIGN (${MAXPAGESIZE}, ${COMMONPAGESIZE})"
  DATA_SEGMENT_END=". = DATA_SEGMENT_END (.);"
fi
INTERP=".interp       ${RELOCATING-0} : { *(.interp) }"
PLT=".plt          ${RELOCATING-0} : { *(.plt) }"
test -z "$GOT" && GOT=".got          ${RELOCATING-0} : {${RELOCATING+ *(.got.plt)} *(.got) }"
DYNAMIC=".dynamic      ${RELOCATING-0} : { *(.dynamic) }"
RODATA=".rodata       ${RELOCATING-0} : { *(.rodata${RELOCATING+ .rodata.* .gnu.linkonce.r.*}) }"
STACKNOTE="/DISCARD/ : { *(.note.GNU-stack) }"
if test -z "${NO_SMALL_DATA}"; then
  SBSS=".sbss         ${RELOCATING-0} :
  {
    ${RELOCATING+PROVIDE (__sbss_start = .);}
    ${RELOCATING+PROVIDE (___sbss_start = .);}
    ${RELOCATING+*(.dynsbss)}
    *(.sbss${RELOCATING+ .sbss.* .gnu.linkonce.sb.*})
    ${RELOCATING+*(.scommon)}
    ${RELOCATING+PROVIDE (__sbss_end = .);}
    ${RELOCATING+PROVIDE (___sbss_end = .);}
  }"
  SBSS2=".sbss2        ${RELOCATING-0} : { *(.sbss2${RELOCATING+ .sbss2.* .gnu.linkonce.sb2.*}) }"
  SDATA="/* We want the small data sections together, so single-instruction offsets
     can access them all, and initialized data all before uninitialized, so
     we can shorten the on-disk segment size.  */
  .sdata        ${RELOCATING-0} :
  {
    ${RELOCATING+${SDATA_START_SYMBOLS}}
    *(.sdata${RELOCATING+ .sdata.* .gnu.linkonce.s.*})
  }"
  SDATA2=".sdata2       ${RELOCATING-0} : { *(.sdata2${RELOCATING+ .sdata2.* .gnu.linkonce.s2.*}) }"
  REL_SDATA=".rel.sdata    ${RELOCATING-0} : { *(.rel.sdata${RELOCATING+ .rel.sdata.* .rel.gnu.linkonce.s.*}) }
  .rela.sdata   ${RELOCATING-0} : { *(.rela.sdata${RELOCATING+ .rela.sdata.* .rela.gnu.linkonce.s.*}) }"
  REL_SBSS=".rel.sbss     ${RELOCATING-0} : { *(.rel.sbss${RELOCATING+ .rel.sbss.* .rel.gnu.linkonce.sb.*}) }
  .rela.sbss    ${RELOCATING-0} : { *(.rela.sbss${RELOCATING+ .rela.sbss.* .rela.gnu.linkonce.sb.*}) }"
  REL_SDATA2=".rel.sdata2   ${RELOCATING-0} : { *(.rel.sdata2${RELOCATING+ .rel.sdata2.* .rel.gnu.linkonce.s2.*}) }
  .rela.sdata2  ${RELOCATING-0} : { *(.rela.sdata2${RELOCATING+ .rela.sdata2.* .rela.gnu.linkonce.s2.*}) }"
  REL_SBSS2=".rel.sbss2    ${RELOCATING-0} : { *(.rel.sbss2${RELOCATING+ .rel.sbss2.* .rel.gnu.linkonce.sb2.*}) }
  .rela.sbss2   ${RELOCATING-0} : { *(.rela.sbss2${RELOCATING+ .rela.sbss2.* .rela.gnu.linkonce.sb2.*}) }"
fi
INIT_ARRAY=".init_array   ${RELOCATING-0} :
  {
    ${RELOCATING+${CREATE_SHLIB-PROVIDE (__init_array_start = .);}}
    KEEP (*(SORT(.init_array.*)))
    KEEP (*(.init_array))
    ${RELOCATING+${CREATE_SHLIB-PROVIDE (__init_array_end = .);}}
  }"
FINI_ARRAY=".fini_array   ${RELOCATING-0} :
  {
    ${RELOCATING+${CREATE_SHLIB-PROVIDE (__fini_array_start = .);}}
    KEEP (*(SORT(.fini_array.*)))
    KEEP (*(.fini_array))
    ${RELOCATING+${CREATE_SHLIB-PROVIDE (__fini_array_end = .);}}
  }"
CTOR=".ctors        ${CONSTRUCTING-0} :
  {
    ${CONSTRUCTING+${CTOR_START}}
    /* gcc uses crtbegin.o to find the start of
       the constructors, so we make sure it is
       first.  Because this is a wildcard, it
       doesn't matter if the user does not
       actually link against crtbegin.o; the
       linker won't look for a file to match a
       wildcard.  The wildcard also means that it
       doesn't matter which directory crtbegin.o
       is in.  */

    KEEP (*crtbegin*.o(.ctors))

    /* We don't want to include the .ctor section from
       from the crtend.o file until after the sorted ctors.
       The .ctor section from the crtend file contains the
       end of ctors marker and it must be last */

    KEEP (*(EXCLUDE_FILE (*crtend*.o $OTHER_EXCLUDE_FILES) .ctors))
    KEEP (*(SORT(.ctors.*)))
    KEEP (*(.ctors))
    ${CONSTRUCTING+${CTOR_END}}
  }"
DTOR=".dtors        ${CONSTRUCTING-0} :
  {
    ${CONSTRUCTING+${DTOR_START}}
    KEEP (*crtbegin*.o(.dtors))
    KEEP (*(EXCLUDE_FILE (*crtend*.o $OTHER_EXCLUDE_FILES) .dtors))
    KEEP (*(SORT(.dtors.*)))
    KEEP (*(.dtors))
    ${CONSTRUCTING+${DTOR_END}}
  }"
STACK="  .stack        ${RELOCATING-0}${RELOCATING+${STACK_ADDR}} :
  {
    ${RELOCATING+_stack = .;}
    *(.stack)
  }"

TEXT_START_ADDR="SEGMENT_START(\"text-segment\", ${TEXT_START_ADDR})"
SHLIB_TEXT_START_ADDR="SEGMENT_START(\"text-segment\", ${SHLIB_TEXT_START_ADDR:-0})"

# if this is for an embedded system, don't add SIZEOF_HEADERS.
if [ -z "$EMBEDDED" ]; then
   test -z "${TEXT_BASE_ADDRESS}" && TEXT_BASE_ADDRESS="${TEXT_START_ADDR} + SIZEOF_HEADERS"
else
   test -z "${TEXT_BASE_ADDRESS}" && TEXT_BASE_ADDRESS="${TEXT_START_ADDR}"
fi

cat <<EOF
OUTPUT_FORMAT("${OUTPUT_FORMAT}", "${BIG_OUTPUT_FORMAT}",
	      "${LITTLE_OUTPUT_FORMAT}")
OUTPUT_ARCH(${OUTPUT_ARCH})
${RELOCATING+ENTRY(${ENTRY})}

${RELOCATING+${LIB_SEARCH_DIRS}}
${RELOCATING+/* Do we need any of these for elf?
   __DYNAMIC = 0; ${STACKZERO+${STACKZERO}} ${SHLIB_PATH+${SHLIB_PATH}}  */}
${RELOCATING+${EXECUTABLE_SYMBOLS}}
${RELOCATING+${INPUT_FILES}}
${RELOCATING- /* For some reason, the Solaris linker makes bad executables
  if gld -r is used and the intermediate file has sections starting
  at non-zero addresses.  Could be a Solaris ld bug, could be a GNU ld
  bug.  But for now assigning the zero vmas works.  */}

SECTIONS
{
  /* Read-only sections, merged into text segment: */
  ${CREATE_SHLIB-${CREATE_PIE-${RELOCATING+PROVIDE (__executable_start = ${TEXT_START_ADDR}); . = ${TEXT_BASE_ADDRESS};}}}
  ${CREATE_SHLIB+${RELOCATING+. = ${SHLIB_TEXT_START_ADDR:-0} + SIZEOF_HEADERS;}}
  ${CREATE_PIE+${RELOCATING+. = ${SHLIB_TEXT_START_ADDR:-0} + SIZEOF_HEADERS;}}
  ${CREATE_SHLIB-${INTERP}}
  ${INITIAL_READONLY_SECTIONS}
  ${TEXT_DYNAMIC+${DYNAMIC}}
  .hash         ${RELOCATING-0} : { *(.hash) }
  .dynsym       ${RELOCATING-0} : { *(.dynsym) }
  .dynstr       ${RELOCATING-0} : { *(.dynstr) }
  .gnu.version  ${RELOCATING-0} : { *(.gnu.version) }
  .gnu.version_d ${RELOCATING-0}: { *(.gnu.version_d) }
  .gnu.version_r ${RELOCATING-0}: { *(.gnu.version_r) }

EOF
if [ "x$COMBRELOC" = x ]; then
  COMBRELOCCAT=cat
else
  COMBRELOCCAT="cat > $COMBRELOC"
fi
eval $COMBRELOCCAT <<EOF
  .rel.init     ${RELOCATING-0} : { *(.rel.init) }
  .rela.init    ${RELOCATING-0} : { *(.rela.init) }
  .rel.text     ${RELOCATING-0} : { *(.rel.text${RELOCATING+ .rel.text.* .rel.gnu.linkonce.t.*}) }
  .rela.text    ${RELOCATING-0} : { *(.rela.text${RELOCATING+ .rela.text.* .rela.gnu.linkonce.t.*}) }
  .rel.fini     ${RELOCATING-0} : { *(.rel.fini) }
  .rela.fini    ${RELOCATING-0} : { *(.rela.fini) }
  .rel.rodata   ${RELOCATING-0} : { *(.rel.rodata${RELOCATING+ .rel.rodata.* .rel.gnu.linkonce.r.*}) }
  .rela.rodata  ${RELOCATING-0} : { *(.rela.rodata${RELOCATING+ .rela.rodata.* .rela.gnu.linkonce.r.*}) }
  ${OTHER_READONLY_RELOC_SECTIONS}
  .rel.data     ${RELOCATING-0} : { *(.rel.data${RELOCATING+ .rel.data.* .rel.gnu.linkonce.d.*}) }
  .rela.data    ${RELOCATING-0} : { *(.rela.data${RELOCATING+ .rela.data.* .rela.gnu.linkonce.d.*}) }
  .rel.tdata	${RELOCATING-0} : { *(.rel.tdata${RELOCATING+ .rel.tdata.* .rel.gnu.linkonce.td.*}) }
  .rela.tdata	${RELOCATING-0} : { *(.rela.tdata${RELOCATING+ .rela.tdata.* .rela.gnu.linkonce.td.*}) }
  .rel.tbss	${RELOCATING-0} : { *(.rel.tbss${RELOCATING+ .rel.tbss.* .rel.gnu.linkonce.tb.*}) }
  .rela.tbss	${RELOCATING-0} : { *(.rela.tbss${RELOCATING+ .rela.tbss.* .rela.gnu.linkonce.tb.*}) }
  .rel.ctors    ${RELOCATING-0} : { *(.rel.ctors) }
  .rela.ctors   ${RELOCATING-0} : { *(.rela.ctors) }
  .rel.dtors    ${RELOCATING-0} : { *(.rel.dtors) }
  .rela.dtors   ${RELOCATING-0} : { *(.rela.dtors) }
  .rel.got      ${RELOCATING-0} : { *(.rel.got) }
  .rela.got     ${RELOCATING-0} : { *(.rela.got) }
  ${OTHER_GOT_RELOC_SECTIONS}
  ${REL_SDATA}
  ${REL_SBSS}
  ${REL_SDATA2}
  ${REL_SBSS2}
  .rel.bss      ${RELOCATING-0} : { *(.rel.bss${RELOCATING+ .rel.bss.* .rel.gnu.linkonce.b.*}) }
  .rela.bss     ${RELOCATING-0} : { *(.rela.bss${RELOCATING+ .rela.bss.* .rela.gnu.linkonce.b.*}) }
EOF
if [ -n "$COMBRELOC" ]; then
cat <<EOF
  .rel.dyn      ${RELOCATING-0} :
    {
EOF
sed -e '/^[	 ]*[{}][	 ]*$/d;/:[	 ]*$/d;/\.rela\./d;s/^.*: { *\(.*\)}$/      \1/' $COMBRELOC
cat <<EOF
    }
  .rela.dyn     ${RELOCATING-0} :
    {
EOF
sed -e '/^[	 ]*[{}][	 ]*$/d;/:[	 ]*$/d;/\.rel\./d;s/^.*: { *\(.*\)}/      \1/' $COMBRELOC
cat <<EOF
    }
EOF
fi
cat <<EOF
  .rel.plt      ${RELOCATING-0} : { *(.rel.plt) }
  .rela.plt     ${RELOCATING-0} : { *(.rela.plt) }
  ${OTHER_PLT_RELOC_SECTIONS}

  .init         ${RELOCATING-0} :
  {
    ${RELOCATING+${INIT_START}}
    KEEP (*(SORT_NONE(.init)))
    ${RELOCATING+${INIT_END}}
  } =${NOP-0}

  ${DATA_PLT-${BSS_PLT-${PLT}}}
  .jlitab       ${RELOCATING-0} :
  {
    ${RELOCATING+${JLI_START_TABLE}}
    ${RELOCATING+jlitab*.o(.jlitab*)}
    *(.jlitab${RELOCATING+*})
  } =${NOP-0}
  .text         ${RELOCATING-0} :
  {
    ${RELOCATING+${TEXT_START_SYMBOLS}}
    *(.text .stub${RELOCATING+ .text.* .gnu.linkonce.t.*})
    /* .gnu.warning sections are handled specially by elf.em.  */
    *(.gnu.warning)
    ${RELOCATING+${OTHER_TEXT_SECTIONS}}
  } =${NOP-0}
  .text.init ${RELOCATING-0} :
  {
    *(.text.init)
  } =${NOP-0}
  .fini         ${RELOCATING-0} :
  {
    ${RELOCATING+${FINI_START}}
    KEEP (*(SORT_NONE(.fini)))
    ${RELOCATING+${FINI_END}}
  } =${NOP-0}
  ${RELOCATING+PROVIDE (__etext = .);}
  ${RELOCATING+PROVIDE (_etext = .);}
  ${RELOCATING+PROVIDE (etext = .);}
  ${WRITABLE_RODATA-${RODATA}}
  .rodata1      ${RELOCATING-0} : { *(.rodata1) }
  ${CREATE_SHLIB-${SDATA2}}
  ${CREATE_SHLIB-${SBSS2}}
  ${OTHER_READONLY_SECTIONS}
  .eh_frame_hdr ${RELOCATING-0} : { *(.eh_frame_hdr) }
  .gcc_except_table ${RELOCATING-0} : ONLY_IF_RO { *(.gcc_except_table${RELOCATING+ .gcc_except_table.*}) }

  /* Adjust the address for the data segment.  We want to adjust up to
     the same address within the page on the next page up.  */
  ${CREATE_SHLIB-${CREATE_PIE-${RELOCATING+. = ${DATA_ADDR-${DATA_SEGMENT_ALIGN}};}}}
  ${CREATE_SHLIB+${RELOCATING+. = ${SHLIB_DATA_ADDR-${DATA_SEGMENT_ALIGN}};}}
  ${CREATE_PIE+${RELOCATING+. = ${SHLIB_DATA_ADDR-${DATA_SEGMENT_ALIGN}};}}

  /* Exception handling  */
  .gcc_except_table ${RELOCATING-0} : ONLY_IF_RW { *(.gcc_except_table${RELOCATING+ .gcc_except_table.*}) }

  /* Ensure the __preinit_array_start label is properly aligned.  We
     could instead move the label definition inside the section, but
     the linker would then create the section even if it turns out to
     be empty, which isn't pretty.  */
  ${RELOCATING+. = ALIGN(${ALIGNMENT});}
  ${RELOCATING+${CREATE_SHLIB-PROVIDE (__preinit_array_start = .);}}
  .preinit_array   ${RELOCATING-0} : { *(.preinit_array) }
  ${RELOCATING+${CREATE_SHLIB-PROVIDE (__preinit_array_end = .);}}
  ${RELOCATING+${INIT_ARRAY}}
  ${RELOCATING+${FINI_ARRAY}}

  .data         ${RELOCATING-0} :
  {
    ${RELOCATING+${DATA_START_SYMBOLS}}
    *(.data${RELOCATING+ .data.* .gnu.linkonce.d.*})
    ${CONSTRUCTING+SORT(CONSTRUCTORS)}
  }
  .data.init  ${RELOCATING-0} :
  {
    *(.data.init)
  }
  .data1        ${RELOCATING-0} : { *(.data1) }
  .tdata	${RELOCATING-0} : { *(.tdata${RELOCATING+ .tdata.* .gnu.linkonce.td.*}) }
  .tbss		${RELOCATING-0} : { *(.tbss${RELOCATING+ .tbss.* .gnu.linkonce.tb.*})${RELOCATING+ *(.tcommon)} }
  .eh_frame     ${RELOCATING-0} : { KEEP (*(.eh_frame)) }
  .gcc_except_table ${RELOCATING-0} : { *(.gcc_except_table) }
  ${WRITABLE_RODATA+${RODATA}}
  ${OTHER_READWRITE_SECTIONS}
  ${TEXT_DYNAMIC-${DYNAMIC}}
  ${RELOCATING+${CTOR}}
  ${RELOCATING+${DTOR}}
  .jcr          ${RELOCATING-0} : { KEEP (*(.jcr)) }
  ${DATA_PLT+${PLT}}
  ${RELOCATING+${OTHER_GOT_SYMBOLS}}
  ${GOT}
  ${OTHER_GOT_SECTIONS}
  ${CREATE_SHLIB+${SDATA2}}
  ${CREATE_SHLIB+${SBSS2}}
  ${SDATA}
  ${OTHER_SDATA_SECTIONS}
  ${RELOCATING+_edata = .;}
  ${RELOCATING+PROVIDE (edata = .);}
  ${RELOCATING+__bss_start = .;}
  ${RELOCATING+${OTHER_BSS_SYMBOLS}}
  ${SBSS}
  ${BSS_PLT+${PLT}}
  .bss          ${RELOCATING-0} :
  {
    ${RELOCATING+*(.dynbss)}
    *(.bss${RELOCATING+ .bss.* .gnu.linkonce.b.*})
    ${RELOCATING+*(COMMON)
    /* Align here to ensure that the .bss section occupies space up to
       _end.  Align after .bss to ensure correct alignment even if the
       .bss section disappears because there are no input sections.  */
    . = ALIGN(${ALIGNMENT});}
  }
  ${OTHER_BSS_SECTIONS}
  ${RELOCATING+. = ALIGN(${ALIGNMENT});}
  ${RELOCATING+_end = .;}
  ${RELOCATING+${OTHER_BSS_END_SYMBOLS}}
  ${RELOCATING+PROVIDE (end = .);}
  ${RELOCATING+${DATA_SEGMENT_END}}
EOF

test -n "${RELOCATING}" && cat <<EOF
  /* We want to be able to set a default stack / heap size in a dejagnu
     board description file, but override it for selected test cases.
     The options appear in the wrong order to do this with a single symbol -
     ldflags comes after flags injected with per-file stanzas, and thus
     the setting from ldflags prevails.  */
  .heap :
  {
	__start_heap = . ;
	. = . + (DEFINED(__HEAP_SIZE) ? __HEAP_SIZE : (DEFINED(__DEFAULT_HEAP_SIZE) ? __DEFAULT_HEAP_SIZE : 20k)) ;
	__end_heap = . ;
  }

  . = ALIGN(0x8);
  .stack :
  {
	__stack = . ;
	. = . + (DEFINED(__STACK_SIZE) ? __STACK_SIZE : (DEFINED(__DEFAULT_STACK_SIZE) ? __DEFAULT_STACK_SIZE : 64k)) ;
	__stack_top = . ;
  }
EOF

cat <<EOF
  /* Stabs debugging sections.  */
  .stab          0 : { *(.stab) }
  .stabstr       0 : { *(.stabstr) }
  .stab.excl     0 : { *(.stab.excl) }
  .stab.exclstr  0 : { *(.stab.exclstr) }
  .stab.index    0 : { *(.stab.index) }
  .stab.indexstr 0 : { *(.stab.indexstr) }

  .comment       0 : { *(.comment) }
  .note.gnu.build-id : { *(.note.gnu.build-id) }
EOF

. $srcdir/scripttempl/DWARF.sc

cat <<EOF
  /* ARC Extension Sections */
  .arcextmap	  0 : { *(.arcextmap.*) }

  ${OTHER_SECTIONS}
  ${RELOCATING+${OTHER_END_SYMBOLS}}
  ${RELOCATING+${STACKNOTE}}
}
EOF
