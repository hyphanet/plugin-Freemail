#ifdef WIN32
#include "windows.h"
void _c_die()
{
	ExitProcess(0);
}
#else
#include <stdlib.h>
void _c_die()
{
	exit(0);
}
#endif
